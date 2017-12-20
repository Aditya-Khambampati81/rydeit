package com.rydeit.uilibrary;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Vector;

/**
 *
 */
public abstract class MyAbstractPauseHandler extends Handler {

    final Vector<Message> mQueue = new Vector<Message>();

    private boolean mPaused;

    final public void resume() {
        mPaused = false;
        while (mQueue.size() > 0) {
            Message msg = mQueue.elementAt(0);
            mQueue.removeElementAt(0);
            sendMessage(msg);
        }
    }

    public MyAbstractPauseHandler(Looper l)
    {
        super(l);
    }
    final public void pause() {
        mPaused = true;
    }

    /**
     * mPaused accessor.
     * @return mPaused
     */
    public boolean isPaused() {
        return mPaused;
    }

    /** {@inheritDoc} */
    @Override
    final public void handleMessage(Message message) {
        if(message != null) {
            if (mPaused) {
                Message m = new Message();
                m.copyFrom(message);
                mQueue.add(m);
            } else {
                processMessage(message);
            }
        }
    }

    protected abstract void processMessage(Message message);
}
