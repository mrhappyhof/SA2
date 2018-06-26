package com.example.maxschwarz.sa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConnectFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConnectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConnectFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public ConnectFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ConnectFragment newInstance() {
        ConnectFragment fragment = new ConnectFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View rootView=inflater.inflate(R.layout.fragment_connect, container, false);
        rootView.findViewById(R.id.c_btn_disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.c_btn_disconnect) {
                    btnDisconnect(v);
                }
            }
        });
        TextView c_txt_URL=rootView.findViewById(R.id.c_txt_connectedToOutput);
        TextView c_txt_DB=rootView.findViewById(R.id.c_txt_selectedDatabaseOutput);
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(getContext());
        c_txt_URL.setText(pref.getString("URL","**********"));
        c_txt_DB.setText(pref.getString("DB","**********"));
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public void btnDisconnect(View v){
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor prefEditor=pref.edit();
        prefEditor.putString("DB_URL","");
        prefEditor.putString("DB","");
        prefEditor.putString("URL","");
        prefEditor.putString("USER","");
        prefEditor.putString("PASS","");
        startActivity(new Intent(getContext(), Login.class));
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
