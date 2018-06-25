package com.example.maxschwarz.sa;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Login extends AppCompatActivity {
    EditText txt_dburl;
    EditText txt_db;
    EditText txt_user;
    EditText txt_pass;
    CheckBox chkbx;
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
        new sql(this).execute(DBURL,USER,PASS);
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
            //Exception
        }
    }

}
class sql extends AsyncTask<String, Void, Boolean> {
    private ProgressDialog progressDialog;
    private Login m;
    private String DB_URL;
    private String USER;
    private String PASS;
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
            DatabaseMetaData meta = conn.getMetaData();/*/
            rs = meta.getTables(null, null, "main",
                    null);
            if(rs.next()){
                rs = meta.getTables(null, null, "users",
                        null);
                if(rs.next()){
                    rs = meta.getTables(null, null, "material",
                            null);
                    if(rs.next()){
                        stmt=conn.prepareStatement("Select version From material");
                        rs.next();
                        if(rs.getString("version")=="1.0.0"){
                            stmt=conn.prepareStatement("Select perm From users where user='"+USER+"';");
                            rs=stmt.executeQuery();
                            if(rs.next()){
                                if(rs.getInt("perm")!=0){
                                    permission=rs.getInt("perm");
                                    return true;
                                }else{
                                    Exception("ErrorComunication");
                                    return false;
                                }
                            }else{
                                Exception("User not existing");
                                return false;
                            }

                        }
                    }else{
                        //Permissions:
                        //1: Admin
                        //2: Stuffguy
                        //3:Massages add
                        //4: Normal
                        //5: just see
                        stmt=conn.prepareStatement("CREATE TABLE material ( id INT(11) AUTO_INCREMENT PRIMARY KEY, version VARCHAR(5) NOT NULL, lastUpdate default now() NOT NULL);");
                        if(stmt.execute()){
                            stmt=conn.prepareStatement("Insert INTO material (version) VALUES (1.0.0)");
                            stmt.execute();
                            stmt=conn.prepareStatement("Select perm From users where user='"+USER+"';");
                            rs=stmt.executeQuery();
                            if(rs.next()){
                                if(rs.getInt("perm")!=0){
                                    permission=rs.getInt("perm");
                                    return true;
                                }else{
                                    Exception("ErrorComunication");
                                    return false;
                                }
                            }else{
                                Exception("ErrorComunication");
                                return false;
                            }

                        }else{
                            Exception("ErrorComunication");
                            return false;
                        }
                    }
                }else{
                    stmt=conn.prepareStatement("CREATE TABLE users ( id INT(6) AUTO_INCREMENT PRIMARY KEY, user VARCHAR(30) NOT NULL, massages tinyint(1) NOT NULL default 0,perm INT(1);");
                    if(stmt.execute()){
                        stmt=conn.prepareStatement("Insert INTO users (user,perm) Values ("+USER+",1);");
                        if(stmt.execute()){
                            rs = meta.getTables(null, null, "material",
                                    null);
                            if(rs.next()){
                                stmt=conn.prepareStatement("Select version From material");
                                rs.next();
                                if(rs.getString("version")=="1.0.0"){
                                    stmt=conn.prepareStatement("Select perm From users where user='"+USER+"';");
                                    rs=stmt.executeQuery();
                                    if(rs.next()){
                                        if(rs.getInt("perm")!=0){
                                            permission=rs.getInt("perm");
                                            return true;
                                        }else{
                                            Exception("ErrorComunication");
                                            return false;
                                        }
                                    }else{
                                        Exception("User not existing");
                                        return false;
                                    }

                                }
                            }else{
                                //Permissions:
                                //1: Admin
                                //2: Stuffguy
                                //3:Massages add
                                //4: Normal
                                //5: just see
                                stmt=conn.prepareStatement("CREATE TABLE material ( id INT(11) AUTO_INCREMENT PRIMARY KEY, version VARCHAR(5) NOT NULL, lastUpdate default now() NOT NULL);");
                                if(stmt.execute()){
                                    stmt=conn.prepareStatement("Insert INTO material (version) VALUES (1.0.0)");
                                    stmt.execute();
                                    stmt=conn.prepareStatement("Select perm From users where user='"+USER+"';");
                                    rs=stmt.executeQuery();
                                    if(rs.next()){
                                        if(rs.getInt("perm")!=0){
                                            permission=rs.getInt("perm");
                                            return true;
                                        }else{
                                            Exception("ErrorComunication");
                                            return false;
                                        }
                                    }else{
                                        Exception("ErrorComunication");
                                        return false;
                                    }

                                }else{
                                    Exception("ErrorComunication");
                                    return false;
                                }
                            }

                        }else{
                            Exception("ErrorComunication");
                            return false;
                        }
                    }else{
                        Exception("ErrorComunication");
                        return false;
                    }
                }
            }else{
                try{
                    stmt=conn.prepareStatement("CREATE TABLE main ( id INT(11) AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), groupe VARCHAR(255), state VARCHAR(255),place VARCHAR(255)img VARCHAR(255),comment VARCHAR(255));");
                    if(stmt.execute()){

                    }else{
                        Exception("ErrorComunication");
                        return false;
                    }
                }catch(Exception e){

                }
            }
            /*/
            try{
                conn.close();
                stmt.close();
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
        progressDialog.dismiss();
        m.Connect(state,DB_URL,USER,PASS,permission);
    }

    private void Exception(String massage){
        //Add exception
    }
}
