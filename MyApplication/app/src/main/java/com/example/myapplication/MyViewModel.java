package com.example.myapplication;

import androidx.lifecycle.ViewModel;

public class MyViewModel extends ViewModel {
    private boolean isReady;

    public MyViewModel() {
        // 초기 데이터 로드 등의 작업을 수행
        isReady = false; // 예: 데이터 로드 중
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    // 추가 데이터 및 메서드 정의
}