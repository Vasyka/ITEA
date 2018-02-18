package com.productions.itea.motivatedev;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GroupTaskDialog extends DialogFragment {


    String description;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.grouptask_dialog, new LinearLayout(getActivity()), false);

        description = getArguments().getString("description");


        ((TextView) view.findViewById(R.id.grouptask_descr)).setText(description);

        Dialog builder = new Dialog(getActivity());
        builder.setContentView(view);
        return builder;

    }

}
