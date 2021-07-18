package my;

import it.tdlight.common.ResultHandler;
import it.tdlight.jni.TdApi;

import java.io.File;

class DefaultHandler implements ResultHandler {
    @Override
    public void onResult(TdApi.Object object) {
        if (object instanceof TdApi.File) {
            if(((TdApi.File) object).local.path.equals("")){
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
                for(TdApi.Message message: messages.messages){
                    if(message.content instanceof TdApi.MessageVoiceNote){
                        TdApi.MessageVoiceNote voice = (TdApi.MessageVoiceNote) message.content;
                        System.out.println("Audio with id "+ voice.voiceNote.voice.id);
                    }
                }
                return;
            }
        }
        Example.print(object.toString());
    }
}
