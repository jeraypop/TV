package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.DialogInfoBinding;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.Util;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Map;

public class InfoDialog {

    private final DialogInfoBinding binding;
    private final Listener callback;
    private AlertDialog dialog;
    private CharSequence title;
    private String header;
    private String url;

    public static InfoDialog create(Activity activity) {
        return new InfoDialog(activity);
    }

    public InfoDialog(Activity activity) {
        this.binding = DialogInfoBinding.inflate(LayoutInflater.from(activity));
        this.callback = (Listener) activity;
    }

    public InfoDialog title(CharSequence title) {
        this.title = title;
        return this;
    }

    public InfoDialog headers(Map<String, String> headers) {
        StringBuilder sb = new StringBuilder();
        for (String key : headers.keySet()) sb.append(key).append(" : ").append(headers.get(key)).append("\n");
        this.header = Util.substring(sb.toString());
        return this;
    }

    public InfoDialog url(String url) {
        this.url = url;
        return this;
    }

    public void show() {
        initDialog();
        initView();
        initEvent();
    }

    private void initDialog() {
        dialog = new MaterialAlertDialogBuilder(binding.getRoot().getContext()).setView(binding.getRoot()).create();
        dialog.getWindow().setDimAmount(0);
        dialog.show();
    }

    private void initView() {
        //新增   不是 http 开头的不显示
        if (fixUrl().toLowerCase().startsWith("http")) {
            binding.downtips.setVisibility(View.VISIBLE);
        } else {
            binding.downtips.setVisibility(View.GONE);
        }
        binding.title.setText(title);
        binding.url.setText(fixUrl());
        binding.header.setText(header);
        binding.url.setVisibility(TextUtils.isEmpty(url) ? View.GONE : View.VISIBLE);
        binding.header.setVisibility(TextUtils.isEmpty(header) ? View.GONE : View.VISIBLE);
    }

    private void initEvent() {
        //新增   不是 http 开头的不分享
        if (fixUrl().toLowerCase().startsWith("http")) {
            binding.url.setOnClickListener(this::onShare);
        }

        binding.url.setOnLongClickListener(v -> onCopy(url));
        binding.header.setOnLongClickListener(v -> onCopy(header));
    }
    private String fixUrl() {
        if (false) {
            return TextUtils.isEmpty(url) ? "" : url.startsWith("data") ? url.substring(0, Math.min(url.length(), 128)).concat("...") : url;
        } else {
            return TextUtils.isEmpty(url) ? "" : url.startsWith("data") ? " 未获取到下载地址": url;
        }

    }

    private void onShare(View view) {
        callback.onShare(title);
        dialog.dismiss();
    }

    private boolean onCopy(String text) {
        Notify.show(R.string.copied);
        Util.copy(text);
        return true;
    }



    public interface Listener {

        void onShare(CharSequence title);
    }
}
