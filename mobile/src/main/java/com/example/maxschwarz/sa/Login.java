package com.example.maxschwarz.sa;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

public class Login extends AppCompatActivity {
    EditText txt_dburl;
    EditText txt_db;
    EditText txt_user;
    EditText txt_pass;
    AsyncTask s;
    CheckBox chkbx;
    Dialog dialog;
    SharedPreferences.Editor prefsEditor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        txt_dburl= (EditText) findViewById(R.id.l_ip_dburl);
        txt_db= (EditText) findViewById(R.id.l_ip_db);
        txt_user= (EditText) findViewById(R.id.l_ip_user);
        txt_pass= (EditText) findViewById(R.id.l_ip_pass);
        chkbx= findViewById(R.id.l_chkbx_StoreLoginData);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        prefsEditor=prefs.edit();
        txt_dburl.setText(prefs.getString("savedDB_URL",""));
        txt_db.setText(prefs.getString("savedDB",""));
        txt_user.setText(prefs.getString("savedUSER",""));
        txt_pass.setText(prefs.getString("savedPASS",""));
        chkbx.setChecked(prefs.getBoolean("savedLogin",false));
    }
    public void btnConnect(View v){
        String DBURL=null;
        String USER=null;
        String PASS=null;
        DBURL="jdbc:mysql://"+txt_dburl.getText().toString()+"/"+txt_db.getText().toString()+"?autoReconnect=true&useUnicode=true&characterEncoding=utf8";
        USER=txt_user.getText().toString();
        PASS=txt_pass.getText().toString();
        s=new sql(this).execute(DBURL,USER,PASS);
    }
    public void Connect(boolean state,String db_url,String user,String pass,int perm){
        if(state==true) {
            Intent intent=new Intent(this, MainActivity.class);

            intent.putExtra("mPerm",perm);
            if(chkbx.isChecked()){
                prefsEditor.putBoolean("savedLogin",true);
               prefsEditor.putString("savedDB_URL",txt_dburl.getText().toString());
               prefsEditor.putString("savedDB",txt_db.getText().toString());
               prefsEditor.putString("savedUSER",txt_user.getText().toString());
               prefsEditor.putString("savedPASS",txt_pass.getText().toString());
            }else{
                prefsEditor.putBoolean("savedLogin",false);
                prefsEditor.putString("savedDB_URL","");
                prefsEditor.putString("savedDB","");
                prefsEditor.putString("savedUSER","");
                prefsEditor.putString("savedPASS","");
            }
            prefsEditor.putString("DB_URL",db_url);
            prefsEditor.putString("USER",user);
            prefsEditor.putString("PASS",pass);
            prefsEditor.putString("URL",txt_dburl.getText().toString());
            prefsEditor.putString("DB",txt_db.getText().toString());
            prefsEditor.putInt("PERM",perm);
            prefsEditor.commit();
            startActivity(intent);
        }else{
            dialog=new Dialog(this);
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

}
class sql extends AsyncTask<String, Void, Boolean> {
    private ProgressDialog progressDialog;
    private Login m;
    private Dialog dialog;
    private String DB_URL;
    private String USER;
    private String PASS;
   ConditionVariable mCondition = new ConditionVariable(true);
    private boolean setupWanted=false;
    int permission=-1;
    protected sql(Login x){
        progressDialog = new ProgressDialog(x);
        m=x;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setMessage("Please Wait");
        progressDialog.setCancelable(false);
        progressDialog.show();
        System.out.println("Initializing Connection");
    }

    protected Boolean doInBackground(String... server_loginData) {
        Connection conn;
        PreparedStatement stmt;
        ResultSet rs;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn= DriverManager.getConnection(server_loginData[0],server_loginData[1],server_loginData[2]);
            stmt=conn.prepareStatement("");
            DB_URL=server_loginData[0];
            USER=server_loginData[1];
            PASS=server_loginData[2];
            System.out.println("Connection possible");
            DatabaseMetaData meta = conn.getMetaData();
            stmt=conn.prepareStatement("SHOW tables");
            rs=stmt.executeQuery();

            //Permissions:
            //0:just see
            //1: normal
            //2: normal + msg
            //3: staff
            //4: admin

            if(rs.next()){
                System.out.println("already setup");
                rs = meta.getTables(null, null, "users",
                        null);
                if(rs.next()){
                    System.out.println("users");
                        rs = meta.getTables(null, null, "material",
                                null);
                    if(rs.next()){
                        System.out.println("material");
                        stmt=conn.prepareStatement("Select version From material");
                        rs=stmt.executeQuery();
                        rs.next();
                        boolean success=false;
                        String version=m.getString(R.string.version);
                        if(rs.getString("version").equals(version)){
                            success=true;
                         }else{
                            if(update(version)){
                                success=true;
                            }
                        }
                        if(success){
                            System.out.println("version");
                        stmt=conn.prepareStatement("Select perm From users where user='"+USER+"';");
                        rs=stmt.executeQuery();
                         if(rs.next()){
                        if(rs.getInt("perm")!=0){
                            permission=rs.getInt("perm");
                            return true;
                        }else{
                            //Exception("ErrorComunication");
                            return false;
                        }
                         }else {
                             //Exception("User not existing");
                             return false;
                         }
                        }
                    }else{
                        defective();
                    }
                }else{
                    defective();
                }
            }else{
                unConfigured();
                return true;

            }
            try{
                conn.close();
                stmt.close();
            }catch(Exception e){

            }
        }catch(Exception e){
            System.out.println("Connection not possible");
            e.printStackTrace();

            return false;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean state) {
        progressDialog.dismiss();
        m.Connect(state,DB_URL,USER,PASS,permission);
    }

    private boolean unConfigured(){
            PreparedStatement stmt=null;
            Connection conn=null;
            m.runOnUiThread(new Runnable() {
            public void run() {
                dialog=new Dialog(m);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.configuration_popup);
                Button cancel=(Button)dialog.findViewById(R.id.conf_btn_cancel);
                Button ok=(Button)dialog.findViewById(R.id.conf_btn_ok);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        myNotify();
                    }
                });
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        myNotify();
                        setupWanted=true;
                    }
                });
                dialog.show();
            }
            });

            try {
                synchronized (this){
                    wait();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            if(setupWanted){
                try {
                    conn = DriverManager.getConnection(DB_URL, USER, PASS);
                    stmt=conn.prepareStatement(
                            "CREATE TABLE `imgsources` (\n" +
                                    "  `imgsource_id` int(20) NOT NULL AUTO_INCREMENT,\n" +
                                    "  `imgsource_source` varchar(255) NOT NULL,\n" +
                                    "  `imgsource_type` varchar(5) NOT NULL,\n" +
                                    "  `imgsource_comment` varchar(255) DEFAULT NULL,\n" +
                                    "PRIMARY KEY (`imgsource_id`)\n"+
                                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
                    stmt.execute();
                    stmt=conn.prepareStatement(
                            "CREATE TABLE `imgs` (\n" +
                                    "  `img_id` int(20) NOT NULL AUTO_INCREMENT,\n" +
                                    "  `img_name` varchar(255) NOT NULL,\n" +
                                    "  `imgsource_id` int(20) NOT NULL,\n" +
                                    "  `img_comment` varchar(255) DEFAULT NULL,\n" +
                                    "PRIMARY KEY (`img_id`),\n"+
                                    "FOREIGN KEY (`imgsource_id`) REFERENCES imgsources(`imgsource_id`)\n"+
                                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1\n");
                    stmt.execute();
                    stmt=conn.prepareStatement(
                            "CREATE TABLE `groups` (\n" +
                                    "  `group_id` int(20) NOT NULL AUTO_INCREMENT,\n" +
                                    "  `group_name` varchar(255) NOT NULL,\n" +
                                    "  `group_comment` varchar(255) DEFAULT NULL,\n" +
                                    "PRIMARY KEY (`group_id`)\n"+
                                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
                    stmt.execute();
                    stmt=conn.prepareStatement(
                            "CREATE TABLE `places` (\n" +
                                    "  `place_id` int(20) NOT NULL AUTO_INCREMENT,\n" +
                                    "  `place_addresse` varchar(255) NOT NULL,\n" +
                                    "  `place_comment` varchar(255) DEFAULT NULL,\n" +
                                    "PRIMARY KEY (`place_id`)\n"+
                                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
                    stmt.execute();
                    stmt=conn.prepareStatement(
                            "CREATE TABLE `states` (\n" +
                                    "  `state_id` int(20) NOT NULL AUTO_INCREMENT,\n" +
                                    "  `state_name` varchar(255) NOT NULL,\n" +
                                    "  `todo_id` int(20) NOT NULL,\n" +
                                    "  `state_comment` varchar(255) DEFAULT NULL,\n" +
                                    "PRIMARY KEY (`state_id`)\n"+
                                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
                    stmt.execute();
                    stmt=conn.prepareStatement(
                            "CREATE TABLE `main` (\n" +
                                    "  `main_id` int(20) NOT NULL AUTO_INCREMENT,\n" +
                                    "  `main_name` varchar(255) NOT NULL,\n" +
                                    "  `group_id` int(20) NOT NULL,\n" +
                                    "  `state_id` int(20) NOT NULL,\n" +
                                    "  `place_id` int(20) NOT NULL,\n" +
                                    "  `main_comment` varchar(255) NOT NULL,\n" +
                                    "  `img_id` int(20) NOT NULL,\n" +
                                    "PRIMARY KEY (`main_id`),\n"+
                                    "FOREIGN KEY (`img_id`) REFERENCES imgs(`img_id`),\n"+
                                    "FOREIGN KEY (`group_id`) REFERENCES groups(`group_id`),\n"+
                                    "FOREIGN KEY (`place_id`) REFERENCES places(`place_id`),\n"+
                                    "FOREIGN KEY (`state_id`) REFERENCES states(`state_id`)\n"+
                                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
                    stmt.execute();
                    stmt=conn.prepareStatement(
                            "CREATE TABLE `users` (\n" +
                                    "  `user_id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                                    "  `user_name` varchar(20) NOT NULL,\n" +
                                    "  `user_permission` int(3) NOT NULL,\n"+
                                    "  `user_comment` varchar(255) DEFAULT NULL,\n" +
                                    "PRIMARY KEY (`user_id`)\n"+
                                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
                    stmt.execute();
                    stmt=conn.prepareStatement(
                            "CREATE TABLE `changes` (\n" +
                            "  `change_id` int(20) NOT NULL AUTO_INCREMENT,\n" +
                            "  `main_id` int(20) NOT NULL,\n" +
                            "  `change_value` varchar(20) NOT NULL,\n" +
                            "  `change_old` varchar(255) NOT NULL,\n" +
                            "  `change_new` varchar(255) NOT NULL,\n" +
                            "  `change_reason` varchar(255) NOT NULL,\n" +
                            "  `change_time` datetime NOT NULL,\n"+
                            "  `user_id` int(20) NOT NULL,\n" +
                            "PRIMARY KEY (`change_id`),\n"+
                            "FOREIGN KEY (main_id) REFERENCES main(main_id),\n"+
                            "FOREIGN KEY (user_id) REFERENCES users(user_id)\n"+
                            ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
                    stmt.execute();
                    stmt=conn.prepareStatement(
                            "CREATE TABLE `config` (\n" +
                            "  `config_sversion` varchar(20) NOT NULL,\n" +
                            "  `config_cversion` varchar(20) NOT NULL,\n" +
                            "  `config_created` datetime NOT NULL,\n" +
                            "  `config_lastUpdate` datetime NOT NULL,\n" +
                            "  `config_imgversion` datetime NOT NULL,\n" +
                            "  `config_ip` varchar(50) NOT NULL,\n" +
                            "PRIMARY KEY (`config_lastUpdate`)\n"+
                            ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
                    stmt.execute();
                    stmt=conn.prepareStatement(
                            "CREATE TABLE `messages` (\n" +
                            "  `message_id` int(20) NOT NULL AUTO_INCREMENT,\n" +
                            "  `message_to` int(20) NOT NULL,\n" +
                            "  `message_from` int(20) NOT NULL,\n" +
                            "  `message_text` varchar(255) NOT NULL,\n" +
                            "  `message_read` tinyint(1) NOT NULL,\n" +
                            "PRIMARY KEY (`message_id`),\n"+
                            "FOREIGN KEY (`message_to`) REFERENCES users(user_id),\n"+
                            "FOREIGN KEY (`message_from`) REFERENCES users(user_id)\n"+
                            ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
                    stmt.execute();
                    stmt=conn.prepareStatement("INSERT INTO users (user_name,user_permission,user_comment) VALUES ('"+USER+"',4,'server creator')");
                    stmt.execute();
                    java.util.Date date=new Date();
                    stmt=conn.prepareStatement("INSERT INTO config (config_sversion,config_cversion,config_created,config_lastUpdate,config_imgversion,config_ip) VALUES ('1.0.0','1.0.0',?,?,?,'1.1.1.1')");
                    stmt.setTimestamp(1, new java.sql.Timestamp(date.getTime()));
                    stmt.setTimestamp(2, new java.sql.Timestamp(date.getTime()));
                    stmt.setTimestamp(3, new java.sql.Timestamp(date.getTime()));
                    stmt.execute();
                    System.out.println(new java.sql.Timestamp(date.getTime()));
                    return true;
                }catch(Exception e){
                    e.printStackTrace();
                    return false;
                }
                finally {
                    try {
                        stmt.close();
                        conn.close();
                    }catch(Exception e){

                    }
                }
            }
            return false;
    }
    private void defective(){
        //Repair -table
    }
    synchronized void unlock(){
        mCondition.open();
    }
    private boolean update(String version){
        //TODO: Add Updates
        return false;
    }
    public void myNotify(){
        synchronized (this){
            notify();
        }
    }
}
