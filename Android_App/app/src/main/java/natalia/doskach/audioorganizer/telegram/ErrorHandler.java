package natalia.doskach.audioorganizer.telegram;

import it.tdlight.common.ExceptionHandler;

class ErrorHandler implements ExceptionHandler {

    @Override
    public void onException(Throwable e) {
        e.printStackTrace();
    }
}
