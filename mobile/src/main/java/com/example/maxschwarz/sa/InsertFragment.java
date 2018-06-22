package com.example.maxschwarz.sa;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import android.widget.EditText;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InsertFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InsertFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InsertFragment extends Fragment {
    private static final String mDB_URL = "";
    private static final String mUSER = "";
    private static final String mPASS = "";
    private static String DB_URL;
    private static String USER;
    private static String PASS;
    private EditText i_ip_name;
    private EditText i_ip_group;
    private EditText i_ip_place;
    private EditText i_ip_state;
    private EditText i_ip_comment;
    private OnFragmentInteractionListener mListener;

    public InsertFragment() {
        // Required empty public constructor
    }


    public static InsertFragment newInstance(String url, String user, String pass) {
        InsertFragment fragment = new InsertFragment();
        Bundle args = new Bundle();
        args.putString(mDB_URL, url);
        args.putString(mUSER, user);
        args.putString(mPASS, pass);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            DB_URL = getArguments().getString(mDB_URL);
            USER = getArguments().getString(mUSER);
            PASS = getArguments().getString(mPASS);
        }
        i_ip_name = (EditText) getView().findViewById(R.id.i_ip_name);
        i_ip_group = (EditText) getView().findViewById(R.id.i_ip_group);
        i_ip_place = (EditText) getView().findViewById(R.id.i_ip_place);
        i_ip_state = (EditText) getView().findViewById(R.id.i_ip_state);
        i_ip_comment = (EditText) getView().findViewById(R.id.i_ip_comment);

    }

    public void btnInsert(View v){
        new InsertSQL(this).execute(DB_URL,USER,PASS,i_ip_name.getText().toString(),i_ip_group.getText().toString(),i_ip_place.getText().toString(),i_ip_state.getText().toString(),i_ip_comment.getText().toString());
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

        return v;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void Success(boolean state){

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
    private ProgressDialog progressDialog;
    private InsertFragment m;
    private String DB_URL;
    private String USER;
    private String PASS;
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
            stmt=conn.prepareStatement("Select * from main");
            rs=stmt.executeQuery();
            rs.next();
            System.out.println(rs.getString(2));
            DB_URL=server_loginData[0];
            USER=server_loginData[1];
            PASS=server_loginData[2];
            System.out.println("Connection possible");
            try{
                stmt.close();
                conn.close();
            }catch(Exception e){

            }
            return true;
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