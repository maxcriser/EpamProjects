package com.maxcriser.cards.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.maxcriser.cards.R;

public class NfcInputDialogBuilder extends AlertDialog {

    private final Context context;
    private ImageView card;
    private final Animation animCard;
    private AlertDialog aDialog;

    public NfcInputDialogBuilder(final Context context) {
        super(context);
        this.context = context;
        this.animCard = AnimationUtils.loadAnimation(context, R.anim.anim_card);
    }

    public void cancelDialog() {
        if (aDialog != null) {
            aDialog.dismiss();
        }
    }

    public void startDialog() {
        final LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.fragment_nfc_input, null);
        final Builder builder = new Builder(context);
        builder.setView(layout);
        aDialog = builder.create();
        aDialog.show();
        card = (ImageView) layout.findViewById(R.id.card);
        card.startAnimation(animCard);
        animCard.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(final Animation pAnimation) {
            }

            @Override
            public void onAnimationEnd(final Animation pAnimation) {
                card.startAnimation(animCard);
            }

            @Override
            public void onAnimationRepeat(final Animation pAnimation) {
            }
        });
    }
}