package natalia.doskach.audioorganizer.telegram;


import org.apache.commons.io.FileUtils;
import org.drinkless.td.libcore.telegram.TdApi;
import org.drinkless.td.libcore.telegram.Client;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Example class for TDLib usage from Java.
 */
public final class Example {
    private static Client client = null;
    private static Context context = null;
    private static Activity activity = null;

    private static TdApi.AuthorizationState authorizationState = null;
    public static volatile boolean haveAuthorization = false;
    private static volatile boolean haveChats = false;
    public static volatile boolean haveChatID = false;
    private static volatile boolean haveAudios = false;
    private static volatile boolean needQuit = false;
    private static volatile boolean canQuit = false;
    public static volatile long chat_id = -1;
    public static volatile long audio_id = -1;
    public static volatile ArrayList<AudioItems> audios;

    private static final Client.ResultHandler defaultHandler = new DefaultHandler();

    private static Lock authorizationLock = new ReentrantLock();
    private static Condition gotAuthorization = authorizationLock.newCondition();

    private static Lock chatsLock = new ReentrantLock();
    private static Condition gotChats = chatsLock.newCondition();

    public static CountDownLatch chatIDLatch = new CountDownLatch(1);
    public static boolean chooseAChat = true;
    public static CountDownLatch audiosLatch = new CountDownLatch(1);
    public static CountDownLatch proceedLatch = new CountDownLatch(1);
    public static CountDownLatch waitForAudioFile = new CountDownLatch(1);

    public static Lock audiosLock = new ReentrantLock();
    public static Condition gotAudios = audiosLock.newCondition();

    private static ConcurrentMap<Integer, TdApi.User> users = new ConcurrentHashMap<Integer, TdApi.User>();
    private static ConcurrentMap<Integer, TdApi.BasicGroup> basicGroups = new ConcurrentHashMap<Integer, TdApi.BasicGroup>();
    private static ConcurrentMap<Integer, TdApi.Supergroup> supergroups = new ConcurrentHashMap<Integer, TdApi.Supergroup>();
    private static ConcurrentMap<Integer, TdApi.SecretChat> secretChats = new ConcurrentHashMap<Integer, TdApi.SecretChat>();

    private static ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<Long, TdApi.Chat>();
    private static NavigableSet<OrderedChat> mainChatList = new TreeSet<OrderedChat>();
    private static boolean haveFullMainChatList = false;


    private static ConcurrentMap<Integer, TdApi.UserFullInfo> usersFullInfo = new ConcurrentHashMap<Integer, TdApi.UserFullInfo>();
    private static ConcurrentMap<Integer, TdApi.BasicGroupFullInfo> basicGroupsFullInfo = new ConcurrentHashMap<Integer, TdApi.BasicGroupFullInfo>();
    private static ConcurrentMap<Integer, TdApi.SupergroupFullInfo> supergroupsFullInfo = new ConcurrentHashMap<Integer, TdApi.SupergroupFullInfo>();

    private static String newLine = System.getProperty("line.separator");
    private static String commandsLine = "Enter command (gcs - GetChats, gc <chatId> - GetChat, me - GetMe, sm <chatId> <message> - SendMessage, lo - LogOut, q - Quit): ";
    private static volatile String currentPrompt = null;
    private static volatile int messageCount = 0;

    static {
        try {
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    public static Client getClient() {
        return client;
    }

    private static void print(String str) {
        if (currentPrompt != null) {
            System.out.println("");
        }
        System.out.println(str);
        if (currentPrompt != null) {
            System.out.print(currentPrompt);
        }
    }

    private static void setChatPositions(TdApi.Chat chat, TdApi.ChatPosition[] positions) {
        synchronized (mainChatList) {
            synchronized (chat) {
                for (TdApi.ChatPosition position : chat.positions) {
                    if (position.list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
                        boolean isRemoved = mainChatList.remove(new OrderedChat(chat.id, position));
                        assert isRemoved;
                    }
                }

                chat.positions = positions;

                for (TdApi.ChatPosition position : chat.positions) {
                    if (position.list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
                        boolean isAdded = mainChatList.add(new OrderedChat(chat.id, position));
                        assert isAdded;
                    }
                }
            }
        }
    }

    private static void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
        if (authorizationState != null) {
            Example.authorizationState = authorizationState;
        }
        switch (Example.authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                parameters.databaseDirectory = context.getFilesDir().getAbsolutePath();
                parameters.useMessageDatabase = false;
                parameters.useSecretChats = false;
                parameters.apiId = 94575;
                parameters.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2";
                parameters.systemLanguageCode = "en";
                parameters.deviceModel = Build.MODEL;
                parameters.applicationVersion = "1.0";
                parameters.enableStorageOptimizer = true;

                client.send(new TdApi.SetTdlibParameters(parameters), new AuthorizationRequestHandler());
                break;
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                client.send(new TdApi.CheckDatabaseEncryptionKey(), new AuthorizationRequestHandler());
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
                Log.i("info", "Please enter phone number: ");
                //               String phoneNumber = "89643500333";
//                String phoneNumber = promptString("Please enter phone number: ");
                //  client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR: {
                String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) Example.authorizationState).link;
                System.out.println("Please confirm this login link on another device: " + link);
                break;
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
                Log.i("info", "Please enter authentication code: ");
                String code = promptString("Please enter authentication code: ");
                client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR: {
                String firstName = promptString("Please enter your first name: ");
                String lastName = promptString("Please enter your last name: ");
                client.send(new TdApi.RegisterUser(firstName, lastName), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR: {
                String password = promptString("Please enter password: ");
                client.send(new TdApi.CheckAuthenticationPassword(password), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                haveAuthorization = true;
                authorizationLock.lock();
                try {
                    gotAuthorization.signal();
                } finally {
                    authorizationLock.unlock();
                }
                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                haveAuthorization = false;
                print("Logging out");
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                haveAuthorization = false;
                print("Closing");
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                print("Closed");
                if (!needQuit) {
                    client = Client.create(new UpdateHandler(), null, null); // recreate client after previous has closed
                } else {
                    canQuit = true;
                }
                break;
            default:
                System.err.println("Unsupported authorization state:" + newLine + Example.authorizationState);
        }
    }

    public static void logout() {
        haveAuthorization = false;
        client.send(new TdApi.LogOut(), defaultHandler);

    }

    private static int toInt(String arg) {
        int result = 0;
        try {
            result = Integer.parseInt(arg);
        } catch (NumberFormatException ignored) {
        }
        return result;
    }

    private static long getChatId(String arg) {
        long chatId = 0;
        try {
            chatId = Long.parseLong(arg);
        } catch (NumberFormatException ignored) {
        }
        return chatId;
    }

    private static String promptString(String prompt) {
        System.out.print(prompt);
        currentPrompt = prompt;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String str = "";
        try {
            str = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentPrompt = null;
        return str;
    }

    private static void getCommand() {
        Log.i("get", "command");
        client.send(new TdApi.GetMe(), defaultHandler);
//        String command = promptString(commandsLine);
//        String[] commands = command.split(" ", 2);
//        try {
//            switch (commands[0]) {
//                case "gcs": {
//                    int limit = 20;
//                    if (commands.length > 1) {
//                        limit = toInt(commands[1]);
//                    }
//                    getMainChatList(limit);
//                    break;
//                }
//                case "gc":
//                    client.send(new TdApi.GetChat(getChatId(commands[1])), defaultHandler);
//                    break;
//                case "me":
//                    client.send(new TdApi.GetMe(), defaultHandler);
//                    break;
//                case "sm": {
//                    String[] args = commands[1].split(" ", 2);
//                    sendMessage(getChatId(args[0]), args[1]);
//                    break;
//                }
//                case "lo":
//                    haveAuthorization = false;
//                    client.send(new TdApi.LogOut(), defaultHandler);
//                    break;
//                case "q":
//                    needQuit = true;
//                    haveAuthorization = false;
//                    client.send(new TdApi.Close(), defaultHandler);
//                    break;
//                default:
//                    System.err.println("Unsupported command: " + command);
//            }
//        } catch (ArrayIndexOutOfBoundsException e) {
//            print("Not enough arguments");
//        }
    }

    private static void getMainChatList(final int limit) {
        synchronized (mainChatList) {
            if (!haveFullMainChatList && limit > mainChatList.size()) {
                // send GetChats request if there are some unknown chatsList and have not enough known chatsList
                long offsetOrder = Long.MAX_VALUE;
                long offsetChatId = 0;
                if (!mainChatList.isEmpty()) {
                    OrderedChat last = mainChatList.last();
                    offsetOrder = last.position.order;
                    offsetChatId = last.chatId;
                }
                client.send(new TdApi.GetChats(new TdApi.ChatListMain(), offsetOrder, offsetChatId, limit - mainChatList.size()), new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        switch (object.getConstructor()) {
                            case TdApi.Error.CONSTRUCTOR:
                                System.err.println("Receive an error for GetChats:" + newLine + object);
                                break;
                            case TdApi.Chats.CONSTRUCTOR:
                                long[] chatIds = ((TdApi.Chats) object).chatIds;
                                if (chatIds.length == 0) {
                                    synchronized (mainChatList) {
                                        haveFullMainChatList = true;
                                    }
                                }
                                // chatsList had already been received through updates, let's retry request
                                getMainChatList(limit);
                                break;
                            default:
                                System.err.println("Receive wrong response from TDLib:" + newLine + object);
                        }
                    }
                });
                return;
            }
            Log.i("start ", "working w/ chats");
            java.util.Iterator<OrderedChat> iter = mainChatList.iterator();
            ArrayList<ChatItems> chatsList = new ArrayList<>();

            //           String header = "First " + limit + " chat(s) out of " + mainChatList.size() + " known chat(s):";
            //           Log.e("IMP",header);
//            TelegramActivity.makeAToast(header);
            for (int i = 0; i < limit && i < mainChatList.size(); i++) {
                long chatId = iter.next().chatId;
                TdApi.Chat chat = chats.get(chatId);
                synchronized (chat) {
                    chatsList.add(new ChatItems(chatId, chat.title));
                    String chatInfo = chatId + ": " + chat.title;
                    //                   TelegramActivity.makeAToast(chatInfo);
                    //                   Log.e("IMP",chatInfo);
                }
            }

            ((TelegramActivity) activity).changeFragmentToChats(chatsList);
            haveChats = true;
            chatsLock.lock();
            try {
                gotChats.signal();
            } finally {
                chatsLock.unlock();
            }

        }
    }

//    private static void sendMessage(long chatId, String message) {
//        // initialize reply markup just for testing
//        TdApi.InlineKeyboardButton[] row = {new TdApi.InlineKeyboardButton("https://telegram.org?1", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?2", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?3", new TdApi.InlineKeyboardButtonTypeUrl())};
//        TdApi.ReplyMarkup replyMarkup = new TdApi.ReplyMarkupInlineKeyboard(new TdApi.InlineKeyboardButton[][]{row, row, row});
//
//        TdApi.InputMessageContent content = new TdApi.InputMessageText(new TdApi.FormattedText(message, null), false, true);
//        client.send(new TdApi.SendMessage(chatId, 0, 0, null, replyMarkup, content), defaultHandler);
//    }

    public static void main(Context con, Activity a) throws InterruptedException {
        audios = new ArrayList<>();
        context = con;
        activity = a;
        haveChats = false;
        haveAudios = false;
        haveAuthorization = false;
        haveFullMainChatList = false;
        chat_id = -1;
        authorizationLock = new ReentrantLock();
        gotAuthorization = authorizationLock.newCondition();

        chatsLock = new ReentrantLock();
        gotChats = chatsLock.newCondition();

        chatIDLatch = new CountDownLatch(1);
        chooseAChat = true;
        audiosLatch = new CountDownLatch(1);
        proceedLatch = new CountDownLatch(1);
        waitForAudioFile = new CountDownLatch(1);

        audiosLock = new ReentrantLock();
        gotAudios = audiosLock.newCondition();

        users = new ConcurrentHashMap<Integer, TdApi.User>();
        basicGroups = new ConcurrentHashMap<Integer, TdApi.BasicGroup>();
        supergroups = new ConcurrentHashMap<Integer, TdApi.Supergroup>();
        secretChats = new ConcurrentHashMap<Integer, TdApi.SecretChat>();

        chats = new ConcurrentHashMap<Long, TdApi.Chat>();
        mainChatList = new TreeSet<OrderedChat>();
        haveFullMainChatList = false;


        usersFullInfo = new ConcurrentHashMap<Integer, TdApi.UserFullInfo>();
        basicGroupsFullInfo = new ConcurrentHashMap<Integer, TdApi.BasicGroupFullInfo>();
        supergroupsFullInfo = new ConcurrentHashMap<Integer, TdApi.SupergroupFullInfo>();

        newLine = System.getProperty("line.separator");
        commandsLine = "Enter command (gcs - GetChats, gc <chatId> - GetChat, me - GetMe, sm <chatId> <message> - SendMessage, lo - LogOut, q - Quit): ";
        String currentPrompt = null;
        int messageCount = 0;
        // disable TDLib log
        Client.execute(new TdApi.SetLogVerbosityLevel(25));
        if (Client.execute(new TdApi.SetLogStream(new TdApi.LogStreamFile(context.getFilesDir().getAbsolutePath() + "/tdlib.log", 1 << 27, false))) instanceof TdApi.Error) {
            throw new IOError(new IOException("Write access to the current directory is required"));
        }

        // create client
        client = Client.create(new UpdateHandler(), null, null);
        // test Client.execute
        defaultHandler.onResult(Client.execute(new TdApi.GetTextEntities("@telegram /test_command https://telegram.org telegram.me @gif @test")));
        logout();
        // main loop
        while (!needQuit) {
            // await authorization
            authorizationLock.lock();
            try {
                while (!haveAuthorization) {
                    gotAuthorization.await();
                }
            } finally {
                authorizationLock.unlock();
            }

            if (haveAuthorization) {
                getMainChatList(20);
                chatsLock.lock();
                try {
                    while (!haveChats) {
                        gotChats.await();
                    }
                } finally {
                    chatsLock.unlock();
                }
                Log.i("got", "chats");
                while (chooseAChat) {
                    chatIDLatch = new CountDownLatch(1);
                    audiosLatch = new CountDownLatch(1);
                    chatIDLatch.await();
                    Log.i("got chatID", Long.toString(chat_id));
                    int left = 100;
                    audios.clear();
                    getAudios(0, left);
                    audiosLatch.await();
                    ((TelegramActivity) a).changeFragmentToAudios(audios);
                    proceedLatch.await();
                }
                Log.i("got", "audioID");
                TdApi.DownloadFile downloadFile = new TdApi.DownloadFile((int) audio_id, 16,
                        0, 0, false);
                client.send(downloadFile, new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        if (object instanceof TdApi.Error) {
                            Log.e("error", ((TdApi.Error) object).message);
                        }
                        if (object instanceof TdApi.File) {
                            //start of download...
                            Log.i("info", "start of download");
                        }

                    }
                });

                waitForAudioFile.await();

            }

            while (!canQuit) {
                Thread.sleep(1);
            }
        }
    }

//    public static void closeClient() {
//        needQuit = true;
//        haveAuthorization = false;
//        client.send(new TdApi.Close(), defaultHandler);
//    }

    private static void getAudios(long lastID, int left) {
        Log.e("getting from", Long.toString(lastID));
        Log.e("left:", String.valueOf(left));
        Log.i("chatID", String.valueOf(chat_id));
        TdApi.Function getChatHistory = new TdApi.GetChatHistory(chat_id, lastID,
                0, left, false);
        Example.getClient().send(getChatHistory, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                if (object instanceof TdApi.Error)
                    Log.e("error", ((TdApi.Error) object).message);
                if (object instanceof TdApi.Messages) {
                    int count = ((TdApi.Messages) object).totalCount;
                    Log.e("got messages:", Integer.toString(count));

                    if (count == 0) {
                        audiosLatch.countDown();
                        return;
                    } else {
                        TdApi.Messages messages = (TdApi.Messages) object;
                        long lastMessageId = messages.messages[count - 1].id;
                        for (TdApi.Message message : messages.messages) {
//                            Log.e("message",message.content.toString());
                            if (message.content instanceof TdApi.MessageVoiceNote) {
                                Date date = new java.util.Date(message.date * 1000L);
                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                String strDate = formatter.format(date);
                                int senderID = ((TdApi.MessageSenderUser) message.sender).userId;
                                TdApi.MessageVoiceNote voice = (TdApi.MessageVoiceNote) message.content;
                                audios.add(new AudioItems(voice.voiceNote.voice.id, senderID, null, strDate));
                            }
                        }
                        Log.i("left is", String.valueOf(left - count));
                        if (left - count > 0)
                            getAudios(lastMessageId, left - count);
                        else {
                            audiosLatch.countDown();
                        }


                    }
                }
            }
        });
    }


    private static class OrderedChat implements Comparable<OrderedChat> {
        final long chatId;
        final TdApi.ChatPosition position;

        OrderedChat(long chatId, TdApi.ChatPosition position) {
            this.chatId = chatId;
            this.position = position;
        }

        @Override
        public int compareTo(OrderedChat o) {
            if (this.position.order != o.position.order) {
                return o.position.order < this.position.order ? -1 : 1;
            }
            if (this.chatId != o.chatId) {
                return o.chatId < this.chatId ? -1 : 1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            OrderedChat o = (OrderedChat) obj;
            return this.chatId == o.chatId && this.position.order == o.position.order;
        }
    }

    protected static class DefaultHandler implements Client.ResultHandler {
//        @Override
//        public void onResult(TdApi.Object object) {
// //           Log.i("got",object.toString());
//            if (object instanceof TdApi.Messages) {
//                if (((TdApi.Messages) object).totalCount >= 20) {
//                    ArrayList<AudioItems> chats = new ArrayList<>();
//                    ((TelegramActivity)context).changeFragmentToAudios(chats);
//                    TdApi.Messages messages = (TdApi.Messages) object;
//                    for (TdApi.Message message : messages.messages) {
//                        if (message.content instanceof TdApi.MessageVoiceNote) {
//                            TdApi.MessageVoiceNote voice = (TdApi.MessageVoiceNote) message.content;
//                            System.out.println("Audio with id " + voice.voiceNote.voice.id);
//                            chats.add(new AudioItems(voice.voiceNote.voice.id,"1","@masha270810","20.07.21 15:19:05"));
//                        }
//                    }
//                    ((TelegramActivity)context).changeFragmentToAudios(chats);
//                }
//                return;
//            }
//        }


        @Override
        public void onResult(TdApi.Object object) {
//            if (object instanceof TdApi.File) {
//                if (((TdApi.File) object).local.path.equals("")) {
//                    return;
//                }
//                System.out.println("Audio was saved here: " + ((TdApi.File) object).local.path);
//                return;
//            }
            if (object instanceof TdApi.Messages) {
                if (((TdApi.Messages) object).totalCount < 1) {  //TODO: limit for messages
                } else {

                    TdApi.Messages messages = (TdApi.Messages) object;
                    messageCount = messages.totalCount;
                    Log.e("got messages:", Integer.toString(messageCount));
                    long lastMessageId = messages.messages[messages.messages.length - 1].id;
                    for (TdApi.Message message : messages.messages) {
                        Log.e("message", message.content.toString());
//                        if (message.content instanceof TdApi.MessageVoiceNote) {
                        Date date = new java.util.Date(message.date * 1000L);
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        String strDate = formatter.format(date);
//                            int senderID = ((TdApi.MessageSenderUser) message.sender).userId;
//                            TdApi.GetUser user = new TdApi.GetUser(senderID);
//                            Example.client.send(user, this);
////                            TdApi.MessageVoiceNote voice = (TdApi.MessageVoiceNote) message.content;
//                            audios.add(new AudioItems(voice.voiceNote.voice.id,senderID,null,strDate));
//                        }
                    }
                }
                return;
            }
            if (object instanceof TdApi.User) {
                System.out.println("I am user " + ((TdApi.User) object).username);
                return;
            }
            Example.print(object.toString());
        }
    }

    private static class UpdateHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.UpdateFile.CONSTRUCTOR:
//                    if(((TdApi.UpdateFile) object))
                    TdApi.File f = ((TdApi.UpdateFile) object).file;
                    if(f.local.isDownloadingCompleted){
                        String path = ((TdApi.UpdateFile) object).file.local.path;
                        File realFile = new File(path);
                        if(realFile.exists())
                            Log.i("file","exists");
                        else
                            Log.e("file"," doesn't exists");
                        copyFiles(path);
                        ((TelegramActivity) activity).returnAudio(path);
                        client.close();
                        waitForAudioFile.countDown();
                    }

                    break;
                case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                    onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                    break;

                case TdApi.UpdateUser.CONSTRUCTOR:
                    TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;
                    users.put(updateUser.user.id, updateUser.user);
                    break;
                case TdApi.UpdateUserStatus.CONSTRUCTOR: {
                    TdApi.UpdateUserStatus updateUserStatus = (TdApi.UpdateUserStatus) object;
                    TdApi.User user = users.get(updateUserStatus.userId);
                    synchronized (user) {
                        user.status = updateUserStatus.status;
                    }
                    break;
                }
                case TdApi.UpdateBasicGroup.CONSTRUCTOR:
                    TdApi.UpdateBasicGroup updateBasicGroup = (TdApi.UpdateBasicGroup) object;
                    basicGroups.put(updateBasicGroup.basicGroup.id, updateBasicGroup.basicGroup);
                    break;
                case TdApi.UpdateSupergroup.CONSTRUCTOR:
                    TdApi.UpdateSupergroup updateSupergroup = (TdApi.UpdateSupergroup) object;
                    supergroups.put(updateSupergroup.supergroup.id, updateSupergroup.supergroup);
                    break;
                case TdApi.UpdateSecretChat.CONSTRUCTOR:
                    TdApi.UpdateSecretChat updateSecretChat = (TdApi.UpdateSecretChat) object;
                    secretChats.put(updateSecretChat.secretChat.id, updateSecretChat.secretChat);
                    break;

                case TdApi.UpdateNewChat.CONSTRUCTOR: {
                    TdApi.UpdateNewChat updateNewChat = (TdApi.UpdateNewChat) object;
                    TdApi.Chat chat = updateNewChat.chat;
                    synchronized (chat) {
                        chats.put(chat.id, chat);

                        TdApi.ChatPosition[] positions = chat.positions;
                        chat.positions = new TdApi.ChatPosition[0];
                        setChatPositions(chat, positions);
                    }
                    break;
                }
                case TdApi.UpdateChatTitle.CONSTRUCTOR: {
                    TdApi.UpdateChatTitle updateChat = (TdApi.UpdateChatTitle) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.title = updateChat.title;
                    }
                    break;
                }
                case TdApi.UpdateChatPhoto.CONSTRUCTOR: {
                    TdApi.UpdateChatPhoto updateChat = (TdApi.UpdateChatPhoto) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.photo = updateChat.photo;
                    }
                    break;
                }
                case TdApi.UpdateChatLastMessage.CONSTRUCTOR: {
                    TdApi.UpdateChatLastMessage updateChat = (TdApi.UpdateChatLastMessage) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.lastMessage = updateChat.lastMessage;
                        setChatPositions(chat, updateChat.positions);
                    }
                    break;
                }
                case TdApi.UpdateChatPosition.CONSTRUCTOR: {
                    TdApi.UpdateChatPosition updateChat = (TdApi.UpdateChatPosition) object;
                    if (updateChat.position.list.getConstructor() != TdApi.ChatListMain.CONSTRUCTOR) {
                        break;
                    }

                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        int i;
                        for (i = 0; i < chat.positions.length; i++) {
                            if (chat.positions[i].list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
                                break;
                            }
                        }
                        TdApi.ChatPosition[] new_positions = new TdApi.ChatPosition[chat.positions.length + (updateChat.position.order == 0 ? 0 : 1) - (i < chat.positions.length ? 1 : 0)];
                        int pos = 0;
                        if (updateChat.position.order != 0) {
                            new_positions[pos++] = updateChat.position;
                        }
                        for (int j = 0; j < chat.positions.length; j++) {
                            if (j != i) {
                                new_positions[pos++] = chat.positions[j];
                            }
                        }
                        assert pos == new_positions.length;

                        setChatPositions(chat, new_positions);
                    }
                    break;
                }
                case TdApi.UpdateChatReadInbox.CONSTRUCTOR: {
                    TdApi.UpdateChatReadInbox updateChat = (TdApi.UpdateChatReadInbox) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.lastReadInboxMessageId = updateChat.lastReadInboxMessageId;
                        chat.unreadCount = updateChat.unreadCount;
                    }
                    break;
                }
                case TdApi.UpdateChatReadOutbox.CONSTRUCTOR: {
                    TdApi.UpdateChatReadOutbox updateChat = (TdApi.UpdateChatReadOutbox) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.lastReadOutboxMessageId = updateChat.lastReadOutboxMessageId;
                    }
                    break;
                }
                case TdApi.UpdateChatUnreadMentionCount.CONSTRUCTOR: {
                    TdApi.UpdateChatUnreadMentionCount updateChat = (TdApi.UpdateChatUnreadMentionCount) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.unreadMentionCount = updateChat.unreadMentionCount;
                    }
                    break;
                }
                case TdApi.UpdateMessageMentionRead.CONSTRUCTOR: {
                    TdApi.UpdateMessageMentionRead updateChat = (TdApi.UpdateMessageMentionRead) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.unreadMentionCount = updateChat.unreadMentionCount;
                    }
                    break;
                }
                case TdApi.UpdateChatReplyMarkup.CONSTRUCTOR: {
                    TdApi.UpdateChatReplyMarkup updateChat = (TdApi.UpdateChatReplyMarkup) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.replyMarkupMessageId = updateChat.replyMarkupMessageId;
                    }
                    break;
                }
                case TdApi.UpdateChatDraftMessage.CONSTRUCTOR: {
                    TdApi.UpdateChatDraftMessage updateChat = (TdApi.UpdateChatDraftMessage) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.draftMessage = updateChat.draftMessage;
                        setChatPositions(chat, updateChat.positions);
                    }
                    break;
                }
                case TdApi.UpdateChatPermissions.CONSTRUCTOR: {
                    TdApi.UpdateChatPermissions update = (TdApi.UpdateChatPermissions) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.permissions = update.permissions;
                    }
                    break;
                }
                case TdApi.UpdateChatNotificationSettings.CONSTRUCTOR: {
                    TdApi.UpdateChatNotificationSettings update = (TdApi.UpdateChatNotificationSettings) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.notificationSettings = update.notificationSettings;
                    }
                    break;
                }
                case TdApi.UpdateChatDefaultDisableNotification.CONSTRUCTOR: {
                    TdApi.UpdateChatDefaultDisableNotification update = (TdApi.UpdateChatDefaultDisableNotification) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.defaultDisableNotification = update.defaultDisableNotification;
                    }
                    break;
                }
                case TdApi.UpdateChatIsMarkedAsUnread.CONSTRUCTOR: {
                    TdApi.UpdateChatIsMarkedAsUnread update = (TdApi.UpdateChatIsMarkedAsUnread) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.isMarkedAsUnread = update.isMarkedAsUnread;
                    }
                    break;
                }
                case TdApi.UpdateChatIsBlocked.CONSTRUCTOR: {
                    TdApi.UpdateChatIsBlocked update = (TdApi.UpdateChatIsBlocked) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.isBlocked = update.isBlocked;
                    }
                    break;
                }
                case TdApi.UpdateChatHasScheduledMessages.CONSTRUCTOR: {
                    TdApi.UpdateChatHasScheduledMessages update = (TdApi.UpdateChatHasScheduledMessages) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.hasScheduledMessages = update.hasScheduledMessages;
                    }
                    break;
                }

                case TdApi.UpdateUserFullInfo.CONSTRUCTOR:
                    TdApi.UpdateUserFullInfo updateUserFullInfo = (TdApi.UpdateUserFullInfo) object;
                    usersFullInfo.put(updateUserFullInfo.userId, updateUserFullInfo.userFullInfo);
                    break;
                case TdApi.UpdateBasicGroupFullInfo.CONSTRUCTOR:
                    TdApi.UpdateBasicGroupFullInfo updateBasicGroupFullInfo = (TdApi.UpdateBasicGroupFullInfo) object;
                    basicGroupsFullInfo.put(updateBasicGroupFullInfo.basicGroupId, updateBasicGroupFullInfo.basicGroupFullInfo);
                    break;
                case TdApi.UpdateSupergroupFullInfo.CONSTRUCTOR:
                    TdApi.UpdateSupergroupFullInfo updateSupergroupFullInfo = (TdApi.UpdateSupergroupFullInfo) object;
                    supergroupsFullInfo.put(updateSupergroupFullInfo.supergroupId, updateSupergroupFullInfo.supergroupFullInfo);
                    break;
                default:
                    // print("Unsupported update:" + newLine + object);
            }
        }

        private void copyFiles(String path) {
            String sourcePath = path;
            File source = new File(sourcePath);
            String name = source.getName();
            String destinationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            File destination = new File(destinationPath,name);
            try
            {
                FileUtils.copyFile(source, destination);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    static class AuthorizationRequestHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    Log.e("Receive an error:", newLine + object);
                    onAuthorizationStateUpdated(null); // repeat last action
                    break;
                case TdApi.Ok.CONSTRUCTOR:
                    // result is already received through UpdateAuthorizationState, nothing to do
                    break;
                default:
                    Log.e("Receive wrong response from TDLib:", newLine + object);
            }
        }
    }
}
