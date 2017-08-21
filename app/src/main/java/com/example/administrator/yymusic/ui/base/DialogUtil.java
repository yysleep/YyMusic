package com.example.administrator.yymusic.ui.base;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.administrator.yymusic.R;

/**
 * Created by Administrator on 2016/5/30.
 * @author yysleep
 */
public class DialogUtil extends Dialog implements View.OnClickListener {
    private OnClickDialogListener listener;
    TextView tvTitle;
    Button btnOne;
    Button btnTwo;

    public DialogUtil(Context context, OnClickDialogListener listener) {
        super(context,R.style.Theme_AudioDialog);
        this.listener = listener;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_music, null);
        tvTitle = (TextView) view.findViewById(R.id.dialog_title_tv);
        btnOne = (Button) view.findViewById(R.id.dialog_one_btn);
        btnTwo = (Button) view.findViewById(R.id.dialog_two_btn);
        btnOne.setOnClickListener(this);
        btnTwo.setOnClickListener(this);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        getWindow().setGravity(Gravity.BOTTOM);
        Window win = getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        lp.width = WindowManager.LayoutParams.FILL_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lp);
        setContentView(view);

    }

    @Override
    public void onClick(View v) {
        listener.onDialogClick(v);
    }

    public interface OnClickDialogListener {
         void onDialogClick(View v);
    }

    public void showFDialog() {
        show();
    }

}
