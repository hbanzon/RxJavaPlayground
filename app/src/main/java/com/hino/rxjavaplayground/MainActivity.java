package com.hino.rxjavaplayground;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.myLabel) TextView textView;

    private Observable<Integer> observable;
    private DisposableObserver<Integer> disposableObserver;
    private int sum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Create an observable
        observable = Observable.create(emitter -> {
            for (int i = 1; i <= 200; i++) {
                SystemClock.sleep(100l); // simulate delay
                emitter.onNext(i);
            }
            emitter.onComplete();
        });
    }

    @OnClick(R.id.subscribeButton)
    public void onButtonClick() {
        new Thread(() -> createNewDisposableObserver()).start();
    }

    private void createNewDisposableObserver() {
        disposableObserver = observable
                .observeOn(AndroidSchedulers.mainThread()) // subscriber observes on the main thread
                .subscribeOn(Schedulers.computation())     // observable is called outside the main thread
                .subscribeWith(
                    new DisposableObserver<Integer>() {
                        @Override
                        public void onNext(Integer integer) {
                            Timber.d(String.format("onNext(): %d", integer));
                            sum += integer;

                            if (sum % 10 == 0) {
                                Timber.d("showing progress update for multiples of 10: %d", sum);
                                textView.setText(String.format("%d...", sum));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.e(e.getMessage(), e);
                        }

                        @Override
                        public void onComplete() {
                            // onComplete() will NOT be called if you dispose
                            Timber.i("Completed with sum: %d", sum);
                            textView.setText(String.format("%d", sum));
                        }
                    }
                );
    }

    @OnClick(R.id.unsubscribeButton)
    public void onUnsubscribeClick() {
        Timber.i("You have un-subscribed!!!");
        if (disposableObserver != null) {
            disposableObserver.dispose();
        }
        textView.setText(String.format("%d", sum));
    }

}
