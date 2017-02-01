package com.example.gziolle.popmovies;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.view.ContextThemeWrapper;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by gziolle on 2/1/2017.
 */

public class AboutDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.about_dialog_view, null);

        TextView picassoTextView = (TextView) dialogView.findViewById(R.id.picasso_text_view);
        if (picassoTextView != null) {
            picassoTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        TextView fabTextView = (TextView) dialogView.findViewById(R.id.fab_text_view);
        if (fabTextView != null) {
            fabTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        TextView mdbTextView = (TextView) dialogView.findViewById(R.id.the_movie_db_text_view);
        if (mdbTextView != null) {
            mdbTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.dialog_style);
        AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);

        builder.setView(dialogView).setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AboutDialogFragment.this.getDialog().cancel();
            }
        });


        return builder.create();
    }
}
