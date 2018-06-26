package com.example.maxschwarz.sa;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InsertFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InsertFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InsertFragment extends Fragment {
    private EditText i_ip_name;
    private EditText i_ip_group;
    private EditText i_ip_place;
    private EditText i_ip_state;
    private EditText i_ip_comment;
    private OnFragmentInteractionListener mListener;
    Dialog dialog;

    public InsertFragment() {
        // Required empty public constructor
    }


    public static InsertFragment newInstance() {
        InsertFragment fragment = new InsertFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void btnInsert(View v){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String DB_URL=pref.getString("DB_URL","");
        String USER=pref.getString("USER","");
        String PASS=pref.getString("PASS","");
        new InsertSQL(this).execute(DB_URL,USER,PASS,i_ip_name.getText().toString(),i_ip_group.getText().toString(),i_ip_state.getText().toString(),i_ip_place.getText().toString(),i_ip_comment.getText().toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_insert, container, false);
        v.findViewById(R.id.i_btn_insert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.i_btn_insert) {
                    btnInsert(v);
                }
            }
        });
        i_ip_name = (EditText) v.findViewById(R.id.i_ip_name);
        i_ip_group = (EditText) v.findViewById(R.id.i_ip_group);
        i_ip_place = (EditText) v.findViewById(R.id.i_ip_place);
        i_ip_state = (EditText) v.findViewById(R.id.i_ip_state);
        i_ip_comment = (EditText) v.findViewById(R.id.i_ip_comment);
        return v;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void Success(boolean state){

        if(state==false){
            dialog=new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.exception_dialog);
            TextView massage=(TextView) dialog.findViewById(R.id.massage);
            massage.setText(getString(R.string.CommunicationError));
            Button ok=(Button)dialog.findViewById(R.id.ok);
            ok.setText("OK");
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
class InsertSQL extends AsyncTask<String, Void, Boolean> {
    private InsertFragment m;
    protected InsertSQL(InsertFragment x){
        m=x;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("Initializing Connection");
    }

    protected Boolean doInBackground(String... server_loginData) {
        Connection conn;
        PreparedStatement stmt;
        ResultSet rs;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn= DriverManager.getConnection(server_loginData[0],server_loginData[1],server_loginData[2]);
            String sql="INSERT into main (name,groupe,state,place,comment) VALUES('"+server_loginData[3]+"','"+server_loginData[4]+"','"+server_loginData[5]+"','"+server_loginData[6]+"','"+server_loginData[7]+"');";
            stmt=conn.prepareStatement(sql);
            System.out.println(sql);
            stmt.execute();
            System.out.println("Connection possible");
            try{
                stmt.close();
                conn.close();
            }catch(Exception e){

            }
            return false;
        }catch(Exception e){
            System.out.println("Connection not possible");
            e.printStackTrace();

            return false;
        }

    }

    @Override
    protected void onPostExecute(Boolean state) {
        m.Success(state);
    }
}