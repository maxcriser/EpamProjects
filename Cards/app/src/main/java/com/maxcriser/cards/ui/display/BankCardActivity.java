package com.maxcriser.cards.ui.display;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.maxcriser.cards.CoreApplication;
import com.maxcriser.cards.R;
import com.maxcriser.cards.async.OwnAsyncTask;
import com.maxcriser.cards.async.task.RemovePhoto;
import com.maxcriser.cards.constant.ListConstants;
import com.maxcriser.cards.database.DatabaseHelper;
import com.maxcriser.cards.database.models.ModelBankCards;
import com.maxcriser.cards.date.DateToView;
import com.maxcriser.cards.dialog.ImageViewerDialogBuilder;
import com.maxcriser.cards.ui.activities.PhotoView;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_BACK_PHOTO;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_BANK;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_CARDHOLDER;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_FRONT_PHOTO;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_ID;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_NUMBER;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_PIN;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_TYPE;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_VALID;
import static com.maxcriser.cards.constant.Extras.EXTRA_VERIFICATION_NUMBER_BANK;

public class BankCardActivity extends Activity {

    private static final String BITMAP_IMAGE = "BitmapImage";
    private final int DELAY_MILLIS = 300;
    private FloatingActionMenu materialDesignFAM;
    private LinearLayout editLinear;
    private ScrollView mScrollView;
    private final Calendar calendar = Calendar.getInstance();
    String firstPhoto;
    String secondPhoto;
    private Boolean showPin = false;
    private OwnAsyncTask sync;
    private TextView editBank;
    private EditText editName;
    private String editNameString;
    private TextView date;
    private LinearLayout linearFrameAction;
    private String idItem;
    private EditText editPin;
    private ImageView eye;
    private DatabaseHelper dbHelper;
    private Animation animScaleDown;
    private Animation animScaleUp;
    EditText editVerificationNumber;
    EditText editCardholder;
    EditText editNumber;
    ImageView ivFrontPhoto;
    ImageView ivBackPhoto;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_bank);
        findViewById(R.id.search_image_toolbar).setVisibility(GONE);
        sync = new OwnAsyncTask();
        initViews();
    }

    DatePickerDialog.OnDateSetListener dateCallBack = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(final DatePicker view, final int year, final int month, final int day) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            DateToView.setDateToCreditView(date, calendar);
        }
    };

    public void onDateClicked(final View view) {
        final DatePickerDialog tpd = new DatePickerDialog(this, dateCallBack,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        tpd.show();
    }

    private void initViews() {
        final Intent creditIntent = getIntent();
        idItem = creditIntent.getStringExtra(EXTRA_BANK_ID);
        final String bank = creditIntent.getStringExtra(EXTRA_BANK_BANK);
        final String verNumber = creditIntent.getStringExtra(EXTRA_VERIFICATION_NUMBER_BANK);
        final String cardholder = creditIntent.getStringExtra(EXTRA_BANK_CARDHOLDER);
        final String number = creditIntent.getStringExtra(EXTRA_BANK_NUMBER);
        final String pin = creditIntent.getStringExtra(EXTRA_BANK_PIN);
        final String valid = creditIntent.getStringExtra(EXTRA_BANK_VALID);
        final String type = creditIntent.getStringExtra(EXTRA_BANK_TYPE);
        firstPhoto = creditIntent.getStringExtra(EXTRA_BANK_FRONT_PHOTO);
        secondPhoto = creditIntent.getStringExtra(EXTRA_BANK_BACK_PHOTO);

        date = (TextView) findViewById(R.id.date);
        editVerificationNumber = (EditText) findViewById(R.id.ver_number);
        editBank = (TextView) findViewById(R.id.title_show_discount);
        editCardholder = (EditText) findViewById(R.id.cardholder);
        final ImageView editType = (ImageView) findViewById(R.id.type_card);
        editNumber = (EditText) findViewById(R.id.number);
        editPin = (EditText) findViewById(R.id.pin);
        editLinear = (LinearLayout) findViewById(R.id.linear_edit_frame_title_discount);
        editName = (EditText) findViewById(R.id.rename_discount_title);

        editBank.setText(bank);
        editCardholder.setText(cardholder);
        editNumber.setText(number);
        editPin.setText(pin);
        editVerificationNumber.setText(verNumber);
        date.setText(valid);
        final Integer typeID;
        switch (type) {
            case ListConstants.Cards.VISA:
                typeID = R.drawable.type_visa;
                break;
            case ListConstants.Cards.MASTERCARD:
                typeID = R.drawable.type_mastercard;
                break;
            case ListConstants.Cards.AMEX:
                typeID = R.drawable.type_amex;
                break;
            case ListConstants.Cards.MAESTRO:
                typeID = R.drawable.type_maestro;
                break;
            case ListConstants.Cards.WESTERN_UNION:
                typeID = R.drawable.type_western_union;
                break;
            case ListConstants.Cards.JCB:
                typeID = R.drawable.type_jcb;
                break;
            case ListConstants.Cards.DINERS_CLUB:
                typeID = R.drawable.type_diners_club;
                break;
            default:
                typeID = R.drawable.type_belcard;
                break;
        }
        editType.setBackgroundResource(typeID);

        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        ivFrontPhoto = (ImageView) findViewById(R.id.front_photo);
        ivBackPhoto = (ImageView) findViewById(R.id.back_photo);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        eye = (ImageView) findViewById(R.id.eye);
        final FloatingActionButton floatingActionButtonDelete = (FloatingActionButton) findViewById(R.id.floating_delete_button);
        final FloatingActionButton floatingActionButtonEdit = (FloatingActionButton) findViewById(R.id.floating_edit_button);
        linearFrameAction = (LinearLayout) findViewById(R.id.linear_frame_actions_discount);
        animScaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down_floating);
        animScaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up_floating);

        registerForContextMenu(materialDesignFAM);

        dbHelper = ((CoreApplication) getApplication()).getDatabaseHelper(this);

        Picasso.with(this).load(Uri.parse(firstPhoto)).placeholder(R.drawable.camera_card_size_light).into(ivFrontPhoto);
        Picasso.with(this).load(Uri.parse(secondPhoto)).placeholder(R.drawable.camera_card_size_light).into(ivBackPhoto);

        /*Picasso.with(this).load(Uri.parse(firstPhoto)).into(new Target() {

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                ivFrontPhoto.setImageBitmap(bitmap);
                firstBitmap = bitmap;

            }

            @Override
            public void onBitmapFailed(final Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(final Drawable placeHolderDrawable) {

            }
        });

        Picasso.with(this).load(Uri.parse(secondPhoto)).into(new Target() {

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                ivBackPhoto.setImageBitmap(bitmap);
                secondBitmap = bitmap;
            }

            @Override
            public void onBitmapFailed(final Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(final Drawable placeHolderDrawable) {

            }
        });
        */

        floatingActionButtonDelete.setOnClickListener(new View.OnClickListener() {

            public void onClick(final View v) {

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BankCardActivity.this);
                alertDialogBuilder.setTitle(R.string.remove);
                alertDialogBuilder
                        .setMessage(R.string.are_you_sure_to_delete)
                        .setCancelable(false)
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog, final int id) {
                                dbHelper.delete(ModelBankCards.class, null, ModelBankCards.ID + " = ?", String.valueOf(idItem));
                                sync.execute(new RemovePhoto(), Uri.parse(firstPhoto), null);
                                sync.execute(new RemovePhoto(), Uri.parse(secondPhoto), null);
                                onBackClicked(null);
                            }
                        });
                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                materialDesignFAM.close(true);
            }
        });
        floatingActionButtonEdit.setOnClickListener(new View.OnClickListener() {

            public void onClick(final View v) {
                // newer
                editCardholder.setEnabled(true);
                editNumber.setEnabled(true);
                editPin.setEnabled(true);
                date.setClickable(true);
                editVerificationNumber.setEnabled(true);
                // end newer
                mScrollView.fullScroll(ScrollView.FOCUS_UP);
                editLinear.setVisibility(VISIBLE);
                editBank.setVisibility(GONE);
                linearFrameAction.setVisibility(VISIBLE);
                editNameString = editBank.getText().toString();
                editName.setText(editNameString);
                materialDesignFAM.close(true);
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        materialDesignFAM.startAnimation(animScaleDown);
                        materialDesignFAM.setVisibility(GONE);
                    }
                }, DELAY_MILLIS);
            }
        });
    }

    public void onBackClicked(final View view) {
        super.onBackPressed();
    }

    public void onCancelClicked(final View view) {
        editLinear.setVisibility(GONE);
        editBank.setVisibility(VISIBLE);
        linearFrameAction.setVisibility(GONE);
        materialDesignFAM.setVisibility(VISIBLE);
        materialDesignFAM.startAnimation(animScaleUp);
    }

    public void onCreateCardClicked(final View view) {
        editNameString = editName.getText().toString();
        final String editCardholderString = editCardholder.getText().toString();
        final String editNumberString = editNumber.getText().toString();
        final String editPinString = editPin.getText().toString();
        final String editValidString = date.getText().toString();
        final String editVerString = editVerificationNumber.getText().toString();
        if (!editNameString.isEmpty() && !editCardholderString.isEmpty() && !editNumberString.isEmpty()
                && !editPinString.isEmpty() && !editValidString.isEmpty() && !editVerString.isEmpty()) {
            editBank.setText(editNameString);
            editLinear.setVisibility(GONE);
            editBank.setVisibility(VISIBLE);
            linearFrameAction.setVisibility(GONE);
            materialDesignFAM.setVisibility(VISIBLE);
            materialDesignFAM.startAnimation(animScaleUp);

            dbHelper.edit(ModelBankCards.class, ModelBankCards.TITLE,
                    editNameString, ModelBankCards.ID, String.valueOf(idItem), null);
            dbHelper.edit(ModelBankCards.class, ModelBankCards.CARDHOLDER,
                    editCardholderString, ModelBankCards.ID, String.valueOf(idItem), null);
            dbHelper.edit(ModelBankCards.class, ModelBankCards.NUMBER,
                    editNumberString, ModelBankCards.ID, String.valueOf(idItem), null);
            dbHelper.edit(ModelBankCards.class, ModelBankCards.PIN,
                    editPinString, ModelBankCards.ID, String.valueOf(idItem), null);
            dbHelper.edit(ModelBankCards.class, ModelBankCards.VALID,
                    editValidString, ModelBankCards.ID, String.valueOf(idItem), null);
            dbHelper.edit(ModelBankCards.class, ModelBankCards.VERIFICATION_NUMBER,
                    editVerString, ModelBankCards.ID, String.valueOf(idItem), null);
            editCardholder.setEnabled(false);
            editNumber.setEnabled(false);
            editPin.setEnabled(false);
            date.setClickable(false);
            editVerificationNumber.setEnabled(false);
        } else {
            mScrollView.fullScroll(ScrollView.FOCUS_UP);
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_LONG).show();
        }
    }

    public void onToolbarBackClicked(final View view) {
    }

    public void onShowPinClicked(final View view) {
        showPin = !showPin;
        if (showPin) {
            editPin.setInputType(TYPE_TEXT_VARIATION_PASSWORD);
            eye.setImageResource(R.drawable.eye_off);
        } else {
            editPin.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
            eye.setImageResource(R.drawable.eye_on);
        }
    }

    void showPhoto(final Bitmap bitmap) {
        final ImageViewerDialogBuilder dialog = new ImageViewerDialogBuilder(this, bitmap);
        dialog.startDialog();
    }

    public void onSecondPhotoClicked(final View view) {
        if (secondPhoto != null) {
//            ivBackPhoto.buildDrawingCache();
//            final Bitmap bitmap = ivBackPhoto.getDrawingCache();
            final Intent intent = new Intent(this, PhotoView.class).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra(BITMAP_IMAGE, secondPhoto);
            startActivity(intent);
        }
    }

    public void onFirstPhotoClicked(final View view) {
        if (firstPhoto != null) {
            final Intent intent = new Intent(this, PhotoView.class).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra(BITMAP_IMAGE, firstPhoto);
            startActivity(intent);
        }
    }
}