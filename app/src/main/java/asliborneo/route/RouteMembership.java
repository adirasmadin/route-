package asliborneo.route;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import asliborneo.route.Model.RouteDriver;
import asliborneo.route.PayServices.CheckoutActivity;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import asliborneo.route.Commons;
import asliborneo.route.Model.RouteDriver;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class RouteMembership extends AppCompatActivity {
    Button btnBack,btnRegister;
    FirebaseAuth auth;
    FirebaseDatabase Driver;
    DatabaseReference users;
    MaterialEditText email,password,name;
    RadioGroup gender;
    RelativeLayout rootlayout;

//    TextView txt_forgot_password;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/antaro.ttf").setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.activity_route_membership);
        auth=FirebaseAuth.getInstance();
        Driver=FirebaseDatabase.getInstance();

        users=Driver.getReference(Commons.Registered_driver);
        Commons.current_routeDriver= new RouteDriver();

//        txt_forgot_password.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                show_forgot_password_dialog();
//            }
//        });
        btnBack=(Button)findViewById(R.id.btn_back);
        btnRegister=(Button)findViewById(R.id.btnRegister);
        rootlayout=(RelativeLayout) findViewById(R.id.rootlayout);
        Paper.init(this);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show_register_dialog();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        String username=Paper.book().read(Commons.user_field);
        String password=Paper.book().read(Commons.password_field);
        if(username!=null&&password!=null){
            if(!TextUtils.isEmpty(username)&&!TextUtils.isEmpty(password)){
                auto_login(username,password);
            }
        }


    }

    private void auto_login(String username, String password) {
        final android.app.AlertDialog waitingdialog=new SpotsDialog(RouteMembership.this);
        waitingdialog.show();
        auth.signInWithEmailAndPassword(username,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseDatabase.getInstance().getReference(Commons.Registered_driver).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Commons.current_routeDriver=dataSnapshot.getValue(RouteDriver.class);
                            waitingdialog.dismiss();
                            Toast.makeText(RouteMembership.this,"Login Sucess",Toast.LENGTH_LONG).show();
                            startActivity(new Intent(RouteMembership.this,Driver_Home.class));
                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                waitingdialog.dismiss();
                Toast.makeText(RouteMembership.this,"Login failed "+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }


    private void show_forgot_password_dialog(){
        AlertDialog.Builder forgot_password_dialog=new AlertDialog.Builder(RouteMembership.this);
        forgot_password_dialog .setTitle("Forgot Password");
        forgot_password_dialog .setMessage("Please Enter Your Email");
        LayoutInflater inflater=LayoutInflater.from(RouteMembership.this);
        View v=inflater.inflate(R.layout.forgot_password_layout,null);
        forgot_password_dialog.setView(v);
        final MaterialEditText emailtxt=(MaterialEditText) v.findViewById(R.id.emailtxt);
        forgot_password_dialog .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                final android.app.AlertDialog waiting_dialog=new SpotsDialog(RouteMembership.this);
                waiting_dialog.show();
                if(!TextUtils.isEmpty(emailtxt.getText().toString())) {
                    auth.sendPasswordResetEmail(emailtxt.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            dialogInterface.dismiss();
                            waiting_dialog.dismiss();
                            Snackbar.make(rootlayout, "Reset Link is Sent to Your Email", Snackbar.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialogInterface.dismiss();
                            waiting_dialog.dismiss();
                            Snackbar.make(rootlayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                }else{
                    waiting_dialog.dismiss();
                    Snackbar.make(rootlayout,"Please Enter Email",Snackbar.LENGTH_LONG).show();
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }




    private void show_register_dialog(){
        final AlertDialog.Builder register_dialog=new AlertDialog.Builder(RouteMembership.this);
        final View v=LayoutInflater.from(RouteMembership.this).inflate(R.layout.register_membership,null);
        email=(MaterialEditText) v.findViewById(R.id.emailtxt);
        password=(MaterialEditText) v.findViewById(R.id.passwordtxt);
        name=(MaterialEditText) v.findViewById(R.id.nametxt);
        gender=(RadioGroup) v.findViewById(R.id.gender);
        final RadioButton male= gender.findViewById(R.id.male);
        final RadioButton female= gender.findViewById(R.id.female);

            if (male.isChecked()) {
                female.setChecked(true);
                Commons.current_routeDriver.setGender("Male");
            }
            else {
                if (female.isChecked())
                    male.setChecked(true);
                    Commons.current_routeDriver.setGender("Female");
            }
        register_dialog.setView(v);
        register_dialog.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(TextUtils.isEmpty(email.getText().toString())){
                    Toast.makeText(RouteMembership.this,"Please Enter Email",Toast.LENGTH_LONG).show();
                }else if (TextUtils.isEmpty(password.getText().toString())){
                    Toast.makeText(RouteMembership.this,"Please Enter Password",Toast.LENGTH_LONG).show();
                }else if (password.getText().toString().length() < 6){
                    Toast.makeText(RouteMembership.this,"Password too short",Toast.LENGTH_LONG).show();
                }
//                else if (TextUtils.isEmpty(gender.s().toString())){
//                    Toast.makeText(RouteMembership.this,"Please Enter Phone",Toast.LENGTH_LONG).show();
//                }
                else if (TextUtils.isEmpty(name.getText().toString())){
                    Toast.makeText(RouteMembership.this,"Please Enter Name",Toast.LENGTH_LONG).show();
                }else{
                    auth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                final RouteDriver user=new RouteDriver();
                                user.setName(name.getText().toString());
                                user.setPassword(password.getText().toString());
                                user.setGender(Commons.current_routeDriver.getGender().toString());
                                user.setEmail(email.getText().toString());
                                user.setAvatarUrl("");
                                user.setRates("0.0");
                                user.setCarType(null);
                                user.setMakePayment("Cash");
                                user.setPassword(null);

                                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                    @Override
                                    public void onSuccess(Account account) {
                                        users.child(account.getId()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(RouteMembership.this,"Registration Sucess",Toast.LENGTH_LONG).show();

                                                show_login_dialog();

                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(AccountKitError accountKitError) {

                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RouteMembership.this,"Registration failed "+e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();

    }



    private void show_login_dialog(){
        AlertDialog.Builder login_dialog=new AlertDialog.Builder(RouteMembership.this);
        login_dialog.setTitle("Sign In");
        login_dialog.setMessage("Use Email to Sign In");
        View v=LayoutInflater.from(RouteMembership.this).inflate(R.layout.layout_login,null);
        email=(MaterialEditText) v.findViewById(R.id.emailtxt);
        password=(MaterialEditText) v.findViewById(R.id.passwordtxt);
        login_dialog.setPositiveButton("Sign In", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(TextUtils.isEmpty(email.getText().toString())){
                    Toast.makeText(RouteMembership.this,"Please Enter Email",Toast.LENGTH_LONG).show();
                }else if (TextUtils.isEmpty(password.getText().toString())){
                    Toast.makeText(RouteMembership.this,"Please Enter Password",Toast.LENGTH_LONG).show();
                }else if (password.getText().toString().length() < 6){
                    Toast.makeText(RouteMembership.this,"Password too short",Toast.LENGTH_LONG).show();
                }else{
                    final android.app.AlertDialog waitingdialog=new SpotsDialog(RouteMembership.this);

                    auth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseDatabase.getInstance().getReference(Commons.Registered_driver).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Paper.book().write(Commons.user_field,email.getText().toString());
                                        Paper.book().write(Commons.password_field,password.getText().toString());
                                        Commons.current_routeDriver=dataSnapshot.getValue(RouteDriver.class);
                                        waitingdialog.dismiss();
                                        Toast.makeText(RouteMembership.this,"Login Sucess",Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(RouteMembership.this,Driver_Home.class));
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(RouteMembership.this,"Login failed "+e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setView(v).show();
    }
}


