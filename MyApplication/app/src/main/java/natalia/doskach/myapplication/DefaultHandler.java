package natalia.doskach.myapplication;

import it.tdlight.common.ResultHandler;
import it.tdlight.jni.TdApi;

import java.text.SimpleDateFormat;
import java.util.Date;

class DefaultHandler implements ResultHandler {

    @Override
    public void onResult(TdApi.Object object) {
        if (object instanceof TdApi.File) {
            if (((TdApi.File) object).local.path.equals("")) {
                return;
            }
            System.out.println("Audio was saved here: " + ((TdApi.File) object).local.path);
            return;
        }
        if (object instanceof TdApi.Messages) {
            if (((TdApi.Messages) object).totalCount < 3) {  //TODO: limit for messages
                return;
            } else {
                TdApi.Messages messages = (TdApi.Messages) object;
                for (TdApi.Message message : messages.messages) {
                    Date date = new Date(message.date * 1000L);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    String strDate = formatter.format(date);
                    int senderID = ((TdApi.MessageSenderUser) message.sender).userId;

                    if (message.content instanceof TdApi.MessageVoiceNote) {
                        TdApi.GetUser user = new TdApi.GetUser(senderID);
                        Example.client.send(user, this);

                        TdApi.MessageVoiceNote voice = (TdApi.MessageVoiceNote) message.content;
                        System.out.println("Audio with id " + voice.voiceNote.voice.id + ", date " + strDate + ", sender " + senderID);
                    }
                }
                return;
            }
        }
        if (object instanceof TdApi.User) {
            System.out.println("I am user " + ((TdApi.User) object).username);
            return;
        }
        Example.print(object.toString());
    }
}
