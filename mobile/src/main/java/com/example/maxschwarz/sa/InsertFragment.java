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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.reginald.editspinner.EditSpinner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;


public class InsertFragment extends Fragment {
    private EditText i_ip_name;
    private EditSpinner i_ip_group;
    private EditSpinner i_ip_place;
    private EditSpinner i_ip_state;
    private EditText i_ip_comment;
    private EditText i_ip_img;
    private EditText i_ip_address;
    private OnFragmentInteractionListener mListener;
    private ArrayList<String> group=new ArrayList<>();
    private ArrayList<String> place=new ArrayList<>();
    private ArrayList<String> state=new ArrayList<>();
    private ArrayList<String> address=new ArrayList<>();
    ArrayAdapter<String> GroupAdapter;
    ArrayAdapter<String> PlaceAdapter;
    ArrayAdapter<String> StateAdapter;
    SharedPreferences pref;
    String DB_URL;
    String USER;
    String PASS;
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
        boolean[] test={false,false,false,false,false};
        String[] testS={getString(R.string.name),getString(R.string.group),getString(R.string.place),getString(R.string.address),getString(R.string.state)};
        if(i_ip_name.getText().toString().trim().isEmpty()){
            test[0]=true;
        }
        if(i_ip_group.getText().toString().trim().isEmpty()){
            test[1]=true;
        }
        if(i_ip_place.getText().toString().trim().isEmpty()){
            test[2]=true;
        }
        if(i_ip_address.getText().toString().trim().isEmpty()){
            test[3]=true;
        }
        if(i_ip_state.getText().toString().trim().isEmpty()){
            test[4]=true;
        }
        boolean succ=true;
        for(int i=0;i<test.length;i++){
            if(test[i]==true){
                succ=false;
            }
        }
        if(succ) {
            new InsertSQL(this).execute(DB_URL, USER, PASS, i_ip_name.getText().toString(), i_ip_group.getText().toString(), i_ip_state.getText().toString(), i_ip_place.getText().toString(), i_ip_comment.getText().toString(), i_ip_img.getText().toString(), i_ip_address.getText().toString());
        }else{
            dialog=new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.exception_dialog);
            TextView massage=(TextView) dialog.findViewById(R.id.massage);
            String msg=getString(R.string.ErrorInsertNull)+" ";
            boolean first=true;
            for(int i=0;i<test.length;i++){
                if(test[i]){
                    if(first){
                        msg+=testS[i];
                        first=false;
                    }else{
                        msg+=", "+testS[i];
                    }
                }
            }
            massage.setText(msg);
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

    public void selectPlace(View v){
        int i=place.indexOf(i_ip_place.getText().toString());
        if(i!=-1){
            String text=address.get(i);
            i_ip_address.setText(text);
            System.out.println(i_ip_address.getText().toString());
        }
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
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        DB_URL=pref.getString("DB_URL","");
        USER=pref.getString("USER","");
        PASS=pref.getString("PASS","");
        GroupAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_expandable_list_item_1, group);
        PlaceAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_expandable_list_item_1, place);
        StateAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_expandable_list_item_1, state);
        i_ip_name = (EditText) v.findViewById(R.id.i_ip_name);
        i_ip_group = (EditSpinner) v.findViewById(R.id.i_ip_group);
        i_ip_group.setAdapter(GroupAdapter);
        i_ip_place = (EditSpinner) v.findViewById(R.id.i_ip_place);
        i_ip_place.setAdapter(PlaceAdapter);
        i_ip_place.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectPlace(view);
            }
        });
        i_ip_state = (EditSpinner) v.findViewById(R.id.i_ip_state);
        i_ip_state.setAdapter(StateAdapter);
        i_ip_comment = (EditText) v.findViewById(R.id.i_ip_comment);
        i_ip_img=(EditText) v.findViewById(R.id.i_ip_img);
        i_ip_address=(EditText)v.findViewById(R.id.i_ip_address);
        updatePreview("groups");
        updatePreview("states");
        updatePreview("places");
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

    public void previewUpdate(String list,String[] values){
        if(values==null){
            values=new String[0];
        }
        switch (list){
            case "groups":
                group.clear();
                Collections.addAll(group,values);
                GroupAdapter.notifyDataSetChanged();
                break;
            case "places":
                place.clear();
                String[] value_place=new String[values.length/2];
                String[] value_address=new String[values.length/2];
                for(int i=0;i<values.length/2;i++){
                    value_place[i]=values[i];
                }
                for(int i=values.length/2;i<values.length;i++){
                    value_address[i-values.length/2]=values[i];
                }
                Collections.addAll(place,value_place);
                Collections.addAll(address,value_address);
                PlaceAdapter.notifyDataSetChanged();
                break;
            case "states":
                state.clear();
                Collections.addAll(state,values);
                StateAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
    public void updatePreview(String table){
        new PreviewSQL(this).execute(DB_URL,USER,PASS,table);
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
            int group=1;
            int state=1;
            int place=1;
            int img=1;
            //group
            stmt=conn.prepareStatement("SELECT group_id FROM groups WHERE group_name='"+server_loginData[4]+"'");
            rs=stmt.executeQuery();
            if(rs.next()){
                group=rs.getInt("group_id");
            }else{
                stmt=conn.prepareStatement("INSERT INTO groups (group_name) VALUES (?)");
                stmt.setString(1,server_loginData[4]);
                stmt.execute();
                stmt=conn.prepareStatement("SELECT group_id FROM groups WHERE group_name='"+server_loginData[4]+"'");
                rs=stmt.executeQuery();
                rs.next();
                group=rs.getInt("group_id");
            }
            //place
            stmt=conn.prepareStatement("SELECT place_id FROM places WHERE place_name='"+server_loginData[5]+"'");
            rs=stmt.executeQuery();
            if(rs.next()){
                place=rs.getInt("place_id");
            }else{
                stmt=conn.prepareStatement("INSERT INTO places (place_name,place_address) VALUES (?,?)");
                stmt.setString(1,server_loginData[5]);
                stmt.setString(2,server_loginData[7]);
                stmt.execute();
                stmt=conn.prepareStatement("SELECT place_id FROM places WHERE place_name='"+server_loginData[5]+"'");
                rs=stmt.executeQuery();
                rs.next();
                place=rs.getInt("place_id");
            }
            //state
            stmt=conn.prepareStatement("SELECT state_id FROM states WHERE state_name='"+server_loginData[6]+"'");
            rs=stmt.executeQuery();
            if(rs.next()){
                state=rs.getInt("state_id");
            }else{
                stmt=conn.prepareStatement("INSERT INTO states (state_name) VALUES (?)");
                stmt.setString(1,server_loginData[6]);
                stmt.execute();
                stmt=conn.prepareStatement("SELECT state_id FROM states WHERE state_name='"+server_loginData[6]+"'");
                rs=stmt.executeQuery();
                rs.next();
                state=rs.getInt("state_id");
            }
            //img
            stmt=conn.prepareStatement("SELECT img_id FROM imgs WHERE img_name='"+server_loginData[4]+"'");
            rs=stmt.executeQuery();
            if(rs.next()){
                img=rs.getInt("img_id");
            }else{
                stmt=conn.prepareStatement("INSERT INTO imgs (img_name) VALUES (?)");
                stmt.setString(1,server_loginData[4]);
                stmt.execute();
                stmt=conn.prepareStatement("SELECT img_id FROM imgs WHERE img_name='"+server_loginData[4]+"'");
                rs=stmt.executeQuery();
                rs.next();
                img=rs.getInt("img_id");
            }
            stmt=conn.prepareStatement("INSERT into main (main_name,group_id,state_id,place_id,main_comment,img_id) VALUES(?,?,?,?,?,?)");
            stmt.setString(1,server_loginData[3]);
            stmt.setInt(2,group);
            stmt.setInt(3,state);
            stmt.setInt(4,place);
            stmt.setString(5,server_loginData[7]);
            if(img!=-1){
            stmt.setInt(6,img);
            }else{
                stmt.setNull(6,java.sql.Types.INTEGER);
            }
            stmt.execute();
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

class PreviewSQL extends AsyncTask<String, Void, Boolean> {
    private InsertFragment m;
    protected PreviewSQL(InsertFragment x){
        m=x;
    }
    String table;
    String[] values;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("Initializing Connection");
    }

    protected Boolean doInBackground(String... server_loginData) {
        Connection conn;
        PreparedStatement stmt;
        ResultSet rs;
        ArrayList<String> list=new ArrayList<String>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn= DriverManager.getConnection(server_loginData[0],server_loginData[1],server_loginData[2]);
            table=server_loginData[3];
            if(table!="places") {
                String sql = "SELECT " + table.substring(0, 5) + "_name" + " FROM " + table;
                stmt = conn.prepareStatement(sql);
                System.out.println(sql);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    list.add(rs.getString(table.substring(0, 5) + "_name"));
                }
                values = (String[]) list.toArray(new String[0]);
            }else{
                String sql = "SELECT " + table.substring(0, 5) + "_name," + table.substring(0, 5) + "_address FROM " + table;
                stmt = conn.prepareStatement(sql);
                System.out.println(sql);
                rs = stmt.executeQuery();
                ArrayList<String> place=new ArrayList<String>();
                while (rs.next()) {
                    list.add(rs.getString(table.substring(0, 5) + "_name"));
                    place.add(rs.getString(table.substring(0, 5) + "_address"));
                }
                Collections.addAll(list,place.toArray(new String[0]));
                values = (String[]) list.toArray(new String[0]);
            }
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
        if(state) {
            m.previewUpdate(table,values);
        }
    }
}