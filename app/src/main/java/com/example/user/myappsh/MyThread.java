package com.example.user.myappsh;

/**
 * Created by user on 2017-06-20.
 */

public class MyThread extends Thread {


   MyImageView m_view = null;

    public MyThread(MyImageView _view) {
        m_view = _view;
    }

    public void run() {
        //   int second = 0;
        while (true) {
            //      second++;
            if (m_view.gameState == m_view.State_GameCarrot) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                m_view.gameState = m_view.STATE_GAMEON;
                m_view.postInvalidate();
            }
        }
    }
}

