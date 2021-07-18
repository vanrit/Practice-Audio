package my;

import it.tdlight.common.ResultHandler;
import it.tdlight.jni.TdApi;

class AuthorizationRequestHandler implements ResultHandler {
    @Override
    public void onResult(TdApi.Object object) {
        switch (object.getConstructor()) {
            case TdApi.Error.CONSTRUCTOR:
                System.err.println("Receive an error:" + Example.newLine + object);
                Example.onAuthorizationStateUpdated(null); // repeat last action
                break;
            case TdApi.Ok.CONSTRUCTOR:
                // result is already received through UpdateAuthorizationState, nothing to do
                break;
            default:
                System.err.println("Receive wrong response from TDLib:" + Example.newLine + object);
        }
    }
}
