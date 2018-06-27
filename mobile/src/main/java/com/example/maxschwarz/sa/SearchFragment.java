package com.example.maxschwarz.sa;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    Dialog dialog;
    TableLayout mTable;
    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_search, container, false);
        mTable=v.findViewById(R.id.s_table);
        v.findViewById(R.id.s_btn_searchPopup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.s_btn_searchPopup) {
                    btnSearch(v);
                }
            }
        });
        headers();
        return v;
    }

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
    public void btnSearch(View v){
        dialog=new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.search_dropdown);
        Button search=(Button)dialog.findViewById(R.id.btn_search);
        Button searchDrop=(Button)dialog.findViewById(R.id.btn_dropdown);
        final EditText id=(EditText) dialog.findViewById(R.id.txt_id);
        final EditText name=(EditText) dialog.findViewById(R.id.txt_name);
        final EditText group=(EditText) dialog.findViewById(R.id.txt_group);
        final EditText state=(EditText) dialog.findViewById(R.id.txt_state);
        final EditText place=(EditText) dialog.findViewById(R.id.txt_place);
        final EditText comment=(EditText) dialog.findViewById(R.id.txt_comment);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String DB_URL=pref.getString("DB_URL","");
        final String USER=pref.getString("USER","");
        final String PASS=pref.getString("PASS","");
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Id="-1";
                if(id.getText().toString().trim().length()>0){
                    Id=id.getText().toString();
                }
                search(DB_URL,USER,PASS,Id,name.getText().toString(),group.getText().toString(),state.getText().toString(),place.getText().toString(),comment.getText().toString());
                dialog.dismiss();
            }
        });
        searchDrop.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              dialog.dismiss();
                                          }
                                      });
        dialog.show();
    }
    public void search(String DB_URL,String USER, String PASS, String id, String name, String group, String state, String place, String comment){
        new SearchSQL(this).execute(DB_URL,USER,PASS,id,name,group,state,place,comment);
    }
    public void Success(ArrayList<myTableRow> table){
        mTable.removeAllViewsInLayout();

        for(int h=0;h<table.size();h++){
            myTableRow row=table.get(h);
            row.printOut();

        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
    public void headers(){
        TableRow tr=new TableRow(getContext());
        tr.setLayoutParams(new TableLayout.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        CheckBox chkbx=new CheckBox(getContext());
        chkbx.setText("");

        tr.addView(chkbx);
        TextView b3=new TextView(getContext());
        b3.setText(getContext().getString(R.string.id));
        b3.setTextColor(Color.BLUE);
        b3.setTextSize(10);
        tr.addView(b3);

        TextView b4=new TextView(getContext());
        b4.setPadding(10, 0, 0, 0);
        b4.setTextSize(10);
        b4.setText(getContext().getString(R.string.name));
        b4.setTextColor(Color.BLUE);
        tr.addView(b4);

        TextView b5=new TextView(getContext());
        b5.setPadding(10, 0, 0, 0);
        b5.setText(getContext().getString(R.string.group));
        b5.setTextColor(Color.BLUE);
        b5.setTextSize(10);
        tr.addView(b5);
        TextView b6=new TextView(getContext());
        b6.setPadding(10, 0, 0, 0);
        b6.setText(getContext().getString(R.string.state));
        b6.setTextColor(Color.BLUE);
        b6.setTextSize(10);
        tr.addView(b6);
        TextView b7=new TextView(getContext());
        b7.setPadding(10, 0, 0, 0);
        b7.setText(getContext().getString(R.string.place));
        b7.setTextColor(Color.BLUE);
        b7.setTextSize(10);
        tr.addView(b7);
        TextView b8=new TextView(getContext());
        b8.setPadding(10, 0, 0, 0);
        b8.setText(getContext().getString(R.string.comment));
        b8.setTextColor(Color.BLUE);
        b8.setTextSize(10);
        tr.addView(b8);
        mTable.addView(tr);

        final View vline = new View(getContext());
        vline.setLayoutParams(new
                TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 2));
        vline.setBackgroundColor(Color.BLUE);
        mTable.addView(vline);
    }
}
class SearchSQL extends AsyncTask<String, Void, ArrayList> {
    private SearchFragment m;
    private ProgressDialog progressDialog;
    boolean first=true;
    protected SearchSQL(SearchFragment x){
        m=x;
        progressDialog = new ProgressDialog(x.getActivity());
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setMessage(m.getString(R.string.PleaseWait));
        progressDialog.setCancelable(false);
        progressDialog.show();
        System.out.println("Initializing Connection");
    }

    protected ArrayList<myTableRow> doInBackground(String... server_loginData) {
        Connection conn;
        PreparedStatement stmt;
        ResultSet rs;
        ArrayList table=new ArrayList();
        try {
            String id = server_loginData[3];
            String name = server_loginData[4];
            String group = server_loginData[5];
            String state = server_loginData[6];
            String place = server_loginData[7];
            String comment = server_loginData[8];
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(server_loginData[0], server_loginData[1], server_loginData[2]);
            String sql = "Select * from main";
            if (id != "-1") {
                sql = sql + first(sql)+" WHERE 'id' LIKE '"+id+"'";
            }
            if(name.trim().length()>0){
                sql = sql + first(sql)+" WHERE 'name' LIKE '"+name+"'";
            }
            if(group.trim().length()>0){
                sql = sql + first(sql)+" WHERE 'groupe' LIKE '"+name+"'";
            }
            if(state.trim().length()>0){
                sql = sql + first(sql)+" WHERE 'state' LIKE '"+name+"'";
            }
            if(place.trim().length()>0){
                sql = sql + first(sql)+" WHERE 'place' LIKE '"+name+"'";
            }
            if(comment.trim().length()>0){
                sql = sql + first(sql)+" WHERE 'comment' LIKE '"+name+"'";
            }
            sql=sql+";";
            stmt=conn.prepareStatement(sql);
            System.out.println(sql);
            rs=stmt.executeQuery();
            System.out.println("Connection possible");
            while(rs.next()){
                table.add(new myTableRow(Integer.parseInt(rs.getString("id")),rs.getString("name"),rs.getString("groupe"),rs.getString("state"),rs.getString("place"),rs.getString("comment")));
            }
            try{
                stmt.close();
                conn.close();
            }catch(Exception e){

            }
            return table;
        }catch(Exception e){
            System.out.println("Connection not possible");
            e.printStackTrace();
            table.clear();
            table.add(new myTableRow(-1,null,null,null,null,null));
            return table;
        }

    }

    @Override
    protected void onPostExecute(ArrayList table) {
        m.Success(table);
    }
    String first(String sql){
        if(first){}else{
            sql=sql+" OR";
            first=false;
        }
        progressDialog.dismiss();
        return sql;
    }
}
class myTableRow{
    private int id;
    private String name;
    private String group;
    private String state;
    private String place;
    private String comment;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public String getState() {
        return state;
    }

    public String getPlace() {
        return place;
    }

    public String getComment() {
        return comment;
    }

    public void printOut(){
        System.out.println(id+","+name+","+group+","+state+","+place+","+comment);
    }

    public myTableRow(int Id, String Name, String Group, String State, String Place, String Comment){
        id=Id;
        name=Name;
        group=Group;
        state=State;
        place=Place;
        comment=Comment;
    }


        }
