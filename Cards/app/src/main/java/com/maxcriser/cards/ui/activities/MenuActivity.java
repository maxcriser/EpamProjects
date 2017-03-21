package com.maxcriser.cards.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import com.maxcriser.cards.BuildConfig;
import com.maxcriser.cards.R;
import com.maxcriser.cards.async.OnResultCallback;
import com.maxcriser.cards.async.OwnAsyncTask;
import com.maxcriser.cards.async.task.JsonParser;
import com.maxcriser.cards.constant.ListConstants;
import com.maxcriser.cards.dialog.AlertCustomDialog;
import com.maxcriser.cards.dialog.NotificationDialogBuilder;
import com.maxcriser.cards.manager.NetworkManager;
import com.maxcriser.cards.manager.ProfileManager;
import com.maxcriser.cards.model.SettingsJson;

import static android.view.View.GONE;
import static com.maxcriser.cards.constant.Extras.EXTRA_CHECK_ITEMS;
import static com.maxcriser.cards.constant.Extras.EXTRA_DISCOUNT_TITLE_TO_ITEMS;
import static com.maxcriser.cards.constant.Extras.EXTRA_NFC_TITLE_TO_ITEMS;
import static com.maxcriser.cards.constant.Extras.EXTRA_TICKETS_TITLE_TO_ITEMS;
import static com.maxcriser.cards.constant.ListConstants.CONFIG;
import static com.maxcriser.cards.constant.ListConstants.CREDIT_CARD;
import static com.maxcriser.cards.constant.ListConstants.MV_MAXCRISER_GMAIL_COM;
import static com.maxcriser.cards.constant.ListConstants.SETUP_PIN;
import static com.maxcriser.cards.constant.ListConstants.TEXT_PLAIN;
import static com.maxcriser.cards.constant.ListConstants.TYPE_LOCKED_SCREEN;
import static com.maxcriser.cards.constant.ListConstants.URL_JSON_SETTINGS;
import static com.maxcriser.cards.constant.ListConstants.playMarketUrl;

public class MenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private OwnAsyncTask sync;
    private SettingsJson mSettingsJson;
    private static final String CKEEPER_RATE = "CKeeper: Rate ";
    private static final String SUPPORT = ": Support";
    private static final String MESSAGE_RFC822 = "message/rfc822";
    private static final String MAILTO = "mailto:";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sync = new OwnAsyncTask();
        initViews();
    }

    private void sendEmail(final String title, final String body, final String emailFrom) {
        final Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setType(MESSAGE_RFC822);
        i.putExtra(Intent.EXTRA_EMAIL, emailFrom);
        i.putExtra(Intent.EXTRA_SUBJECT, title);
        i.putExtra(Intent.EXTRA_TEXT, body);
        i.setData(Uri.parse(MAILTO + MV_MAXCRISER_GMAIL_COM));
        try {
            startActivity(i);
        } catch (final android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.no_email_clients_installed, Toast.LENGTH_SHORT).show();
        }
    }

    private void showRate() {
        String gPlayUrl = playMarketUrl;
        if (mSettingsJson != null) {
            gPlayUrl = mSettingsJson.getGooglePlayUrl();
        }
        final AlertCustomDialog dialog = new AlertCustomDialog(this);
        dialog.setView(R.layout.fragment_rate_app)
                .setTopColorRes(R.color.text_toolbar)
                .setCancelable(true)
                .setIcon(R.drawable.ic_pulse)
                .show();

        final View view = dialog.getAddedView();
        final RatingBar ratingBar = (RatingBar) view.findViewById(R.id.rating);
        final String finalGPlayUrl = gPlayUrl;
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(final RatingBar ratingBar, final float rating, final boolean fromUser) {
                if (rating <= 3) {
                    sendEmail(CKEEPER_RATE + rating + SUPPORT, "", ProfileManager.getUserMail(MenuActivity.this));
                } else {
                    openUrl(finalGPlayUrl);
                }
                dialog.dismiss();
            }
        });
    }

    private void openUrl(final String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
        startActivity(intent);
    }

    public void initViews() {
        try {
            if (NetworkManager.isConnected(this)) {
                sync.execute(new JsonParser(), URL_JSON_SETTINGS + CONFIG, new OnResultCallback<String, Void>() {

                    @Override
                    public void onSuccess(final String pS) {
                        mSettingsJson = new SettingsJson(pS);
                        if (mSettingsJson.isFlagVersion()) {
                            if (!mSettingsJson.getAppVersion().equals(BuildConfig.VERSION_NAME)) {
                                final NotificationDialogBuilder notificationDialogBuilder = new NotificationDialogBuilder(
                                        MenuActivity.this, getString(R.string.update_title),
                                        getString(R.string.notification_update_first),
                                        mSettingsJson.getGooglePlayUrl(), true, true, getString(R.string.ok), getString(R.string.cancel));
                                notificationDialogBuilder.startDialog();
                            }
                        }
                        if (mSettingsJson.isFlagMessage()) {
                            final NotificationDialogBuilder notificationDialogBuilder;
                            if (mSettingsJson.isFlagMessageUrl()) {
                                notificationDialogBuilder = new NotificationDialogBuilder(
                                        MenuActivity.this, mSettingsJson.getTitleMessage(),
                                        mSettingsJson.getMessage(),
                                        mSettingsJson.getUrlMessage(), true, true, getString(R.string.ok), getString(R.string.close));
                            } else {
                                notificationDialogBuilder = new NotificationDialogBuilder(
                                        MenuActivity.this, mSettingsJson.getTitleMessage(),
                                        mSettingsJson.getMessage(),
                                        null, false, true, null, getString(R.string.close));
                            }
                            notificationDialogBuilder.startDialog();
                        }
                    }

                    @Override
                    public void onError(final Exception pE) {
                        Toast.makeText(MenuActivity.this, R.string.cannot_parse_settings, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onProgressChanged(final Void pVoid) {

                    }
                });
            } else {
                Toast.makeText(this, R.string.no_connection_internet, Toast.LENGTH_SHORT).show();
            }
        } catch (final Exception e) {
            Toast.makeText(this, R.string.no_connection_internet, Toast.LENGTH_SHORT).show();
        }
        final CardView credit = (CardView) findViewById(R.id.main_credit_card);
        final CardView discount = (CardView) findViewById(R.id.main_discount_card);
        final CardView tickets = (CardView) findViewById(R.id.main_tickets_card);
        final CardView nfc = (CardView) findViewById(R.id.main_nfc_card);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        final NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            nfc.setVisibility(GONE);
        }

        credit.setOnClickListener(new onClickListener());
        discount.setOnClickListener(new onClickListener());
        tickets.setOnClickListener(new onClickListener());
        nfc.setOnClickListener(new onClickListener());

        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, null,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.nav_pin) {
            final Intent intent = new Intent(this, LockerActivity.class);
            intent.putExtra(TYPE_LOCKED_SCREEN, SETUP_PIN);
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY));
        } else if (id == R.id.drawer_help) {
            Toast.makeText(this, "Show help activity", Toast.LENGTH_SHORT).show();
                /*
                final NotificationDialogBuilder notificationDialogBuilder = new NotificationDialogBuilder(this,
                        getString(R.string.about), mSettingsJson.getAbout(), null, false, false, null, null);
                notificationDialogBuilder.startDialog();
                */
        } else if (id == R.id.nav_share) {
            final String body;
            if (mSettingsJson != null) {
                body = mSettingsJson.getBodyShare();
            } else {
                body = getString(R.string.body_share);
            }
            final Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType(TEXT_PLAIN);
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.ck_application));
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
            startActivity(sharingIntent);
        } else if (id == R.id.send_feedback) {
            startActivity(new Intent(this, FeedbackActivity.class));
        } else if (id == R.id.rate) {
            if (mSettingsJson == null) {
                Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            } else {
                showRate();
            }
        } else if (id == R.id.group) {
            showJoinGroup();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showJoinGroup() {
        final String vkUrlGroup;
        final String googlePlusUrlGroup;
        final String facebookUrlGroup;
        if (mSettingsJson != null) {
            vkUrlGroup = mSettingsJson.getVkUrlGroup();
            googlePlusUrlGroup = mSettingsJson.getGoogleUrlGroup();
            facebookUrlGroup = mSettingsJson.getFacebookUrlGroup();
        } else {
            vkUrlGroup = ListConstants.vkGroupUrl;
            googlePlusUrlGroup = ListConstants.googlePlusGroupUrl;
            facebookUrlGroup = ListConstants.facebookGroupUrl;
        }
        final AlertCustomDialog dialog = new AlertCustomDialog(this);
        dialog.setView(R.layout.fragment_join_group)
                .setTopColorRes(R.color.text_toolbar)
                .setCancelable(true)
                .setListener(R.id.facebook_id, new View.OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        openUrl(facebookUrlGroup);
                        dialog.dismiss();
                    }
                })
                .setListener(R.id.vk_com_id, new View.OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        openUrl(vkUrlGroup);
                        dialog.dismiss();
                    }
                })
                .setListener(R.id.google_plus_id, new View.OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        openUrl(googlePlusUrlGroup);
                        dialog.dismiss();
                    }
                })
                .setIcon(R.drawable.account_multiple_white)
                .show();
    }

    public void onMenuClicked(final View view) {
        drawer.openDrawer(GravityCompat.START);
    }

    public void onToolbarClicked(final View view) {
    }

    private class onClickListener implements View.OnClickListener {

        Intent intent;

        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.main_credit_card:
                    intent = new Intent(MenuActivity.this, LockerActivity.class);
                    intent.putExtra(TYPE_LOCKED_SCREEN, CREDIT_CARD);
                    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY));
                    break;
                case R.id.main_discount_card:
                    intent = new Intent(MenuActivity.this, ItemsActivity.class);
                    intent.putExtra(EXTRA_CHECK_ITEMS, EXTRA_DISCOUNT_TITLE_TO_ITEMS);
                    startActivity(intent);
                    break;
                case R.id.main_nfc_card:
//                    startActivity(new Intent(MenuActivity.this, SignInGoogleActivity.class));
                    intent = new Intent(MenuActivity.this, ItemsActivity.class);
                    intent.putExtra(EXTRA_CHECK_ITEMS, EXTRA_NFC_TITLE_TO_ITEMS);
                    startActivity(intent);
                    break;
                case R.id.main_tickets_card:
                    intent = new Intent(MenuActivity.this, ItemsActivity.class);
                    intent.putExtra(EXTRA_CHECK_ITEMS, EXTRA_TICKETS_TITLE_TO_ITEMS);
                    startActivity(intent);
                    break;
            }
        }
    }
}