package com.example.user.myappsh;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

import static java.lang.System.exit;

/**
 * Created by user on 2017-06-19.
 */

public class MyImageView extends View {

    Bitmap Startimg, gameBack, Winimg, fieldimg, Failimg, plusfield;

    final int BLANK = 0;
    final int thiscarrot = 1;

    Bitmap[] images = new Bitmap[2];  // 투명,당근 이미지

    int[][] board = {{BLANK, BLANK, BLANK, BLANK, BLANK},   //사용자가 터치한 당근 넣을 부분
            {BLANK, BLANK, BLANK, BLANK, BLANK},
            {BLANK, BLANK, BLANK, BLANK, BLANK},
            {BLANK, BLANK, BLANK, BLANK, BLANK},
            {BLANK, BLANK, BLANK, BLANK, BLANK}};

    int[][] reproduce = {{BLANK, BLANK, thiscarrot, BLANK, thiscarrot},  //당근
            {BLANK, BLANK, BLANK, BLANK, BLANK},
            {thiscarrot, BLANK, thiscarrot, BLANK, BLANK},
            {BLANK, BLANK, BLANK,thiscarrot, BLANK},
            {thiscarrot, BLANK, BLANK, thiscarrot, BLANK}};

    Rect[][] rect = new Rect[5][5];

    final int State_Start = 0;
    final int STATE_GAMEON = 1;
    final int State_GameCarrot = 2;
    final int STATE_WIN = 3;
    final int STATE_FAIL = -1;

    int carrotN = 0;

    int gameState = State_Start;


    public MyImageView(Context context) {
        // 초기화가 필요한 내용을 여기서 처리한다.
        super(context);

        // 시스템에서 화면의 사이즈 정보를 가지고 옴 (수정필요 없다)
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

        // 각자 기획한 대로 수정해야 함
        int imageWidth = (screenWidth - 60) / 5;
        int imageHeight = (screenHeight - 500) / 5;

        Bitmap _image = BitmapFactory.decodeResource(getResources(), R.drawable.start);
        Startimg = Bitmap.createScaledBitmap(_image, screenWidth, screenHeight, false);

        _image = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        gameBack = Bitmap.createScaledBitmap(_image, screenWidth, screenHeight, false);

        _image = BitmapFactory.decodeResource(getResources(), R.drawable.field);
        fieldimg = Bitmap.createScaledBitmap(_image, 1000, 1000, false);

        _image = BitmapFactory.decodeResource(getResources(), R.drawable.plus);
        images[1] = Bitmap.createScaledBitmap(_image, 170, 170, false);

        _image = BitmapFactory.decodeResource(getResources(), R.drawable.blank);
        images[0] = Bitmap.createScaledBitmap(_image, 200, 200, false);

        _image = BitmapFactory.decodeResource(getResources(), R.drawable.fieldplus);
        plusfield = Bitmap.createScaledBitmap(_image, 180, 180, false);

        _image = BitmapFactory.decodeResource(getResources(), R.drawable.finish);
        Winimg = Bitmap.createScaledBitmap(_image, screenWidth, screenHeight, false);

        _image = BitmapFactory.decodeResource(getResources(), R.drawable.fail);
        Failimg = Bitmap.createScaledBitmap(_image, screenWidth, screenHeight, false);

        MyThread thread = new MyThread(this);
        thread.start();

        // 셀에 대한 정보 ( 한 칸의 넓이 높이) - 이미지 크기 보다는 약간 크게 하기 위해
        int xOneGrid = 200;
        int yOneGrid = 200;

        // 9장의 이미지가 놓일 자리를 미리 계산할 수 있다.
        // 따라서 rect[3][3]를 미리 생성해서 영역을 계산해 둔다.
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++) {
                int left = 60 + j * xOneGrid;
                int right = left + 200;
                int top = 500 + i * yOneGrid;
                int bottom = top + 200;

                rect[i][j] = new Rect(left, top, right, bottom);

            }



     /*   //랜덤일 때
      for (int i = 0; i < 7; i++){

            Random r = new Random();

            int x = r.nextInt(5);
            int y = r.nextInt(5);

            reproduce[x][y] = thiscarrot;  }   */

    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (gameState == State_Start) {

            canvas.drawBitmap(Startimg, 0, 0, null);

        } else if (gameState == STATE_GAMEON) {

            canvas.drawBitmap(gameBack, 0, 0, null); //기본배경
            canvas.drawBitmap(fieldimg, 48, 500, null); //잔디출력

            // 당근 그리기
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {

                    canvas.drawBitmap(images[board[i][j]],rect[i][j].left, rect[i][j].top, null);

                }
            }

        } else if(gameState==State_GameCarrot)
            {

                canvas.drawBitmap(gameBack, 0, 0, null); //기본배경
                canvas.drawBitmap(fieldimg, 48, 500, null); //잔디출력

                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        if (reproduce[i][j] == thiscarrot) {   //당근 출력
                            canvas.drawBitmap(images[1],60 + i * 200, 507 + j * 200, null);
                        }
                    }
                }

            } else if (gameState == STATE_WIN) {

            canvas.drawBitmap(Winimg, 0, 0, null);

        } else { // (gameState == STATE_FAIL)
            canvas.drawBitmap(Failimg, 0, 0, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (gameState == State_Start) {

            gameState = State_GameCarrot;
            invalidate();

        } else if (gameState == STATE_GAMEON) {

            Rect _rect;
            // 사용자가 제대로 놓았는지
            boolean isPlayerDone = false;

            int tries =0;

            for (int idx = 0; idx < 25; idx++) {
                int i = idx / 5;
                int j = idx % 5;

                _rect = rect[i][j];

                if (_rect.contains((int) event.getX(), (int) event.getY())) {
                   tries++;
                    if (board[i][j] != reproduce[i][j]) {   // board의 내용을 바꿈
                        board[i][j] = thiscarrot;
                        carrotN++;

                        if (carrotN==7) {  //10번 안에 당근 모두 찾음
                                gameState = STATE_WIN;
                                invalidate();
                                return false;

                            /*    if (board == reproduce) {  // 랜덤일 때
                            gameState = STATE_WIN;
                            invalidate();
                            return false;  */

                        }
                        if(tries == 10){  //10번 안에 당근 찾지못함
                            gameState = STATE_FAIL;
                            invalidate();
                            return false;
                        }

                        isPlayerDone = true;
                        break;
                    }else{break;}
                }
            }
            invalidate();

            return false;

        } else if (gameState == State_GameCarrot) {

            gameState = STATE_GAMEON;
            invalidate();

        }else if(gameState == STATE_WIN){
            exit(1);
        }else // (gameState == STATE_FAIL)
            {
            exit(1);
        }
        return  false;
    }

}