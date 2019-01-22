package asliborneo.route.Helpers;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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
import asliborneo.route.Driver_Home;
import asliborneo.route.Commons;
import asliborneo.route.Model.RouteDriver;
import asliborneo.route.R;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import okhttp3.Route;

public class FirebaseHelper {
    AppCompatActivity activity;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference users;

    RelativeLayout root;
    public FirebaseHelper(AppCompatActivity activity){
        this.activity=activity;
        root=activity.findViewById(R.id.rootlayout);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        users=firebaseDatabase.getReference(Commons.Registered_driver);
        if(firebaseAuth.getUid()!=null)loginSuccess();
    }

    public void registerByGoogleAccount(final GoogleSignInAccount account){
        final RouteDriver user=new RouteDriver();
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                RouteDriver post = dataSnapshot.child(account.getId()).getValue(RouteDriver.class);

                if(post==null) showRegisterPhone(user, account);
                else loginSuccess();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void showLoginDialog(){
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(activity);
        alertDialog.setTitle(activity.getResources().getString(R.string.login));
        alertDialog.setMessage(activity.getResources().getString(R.string.fill_fields));

        LayoutInflater inflater=LayoutInflater.from(activity);
        View login_layout=inflater.inflate(R.layout.layout_login, null);
        final MaterialEditText etEmail=login_layout.findViewById(R.id.nametxt);
        final MaterialEditText etPassword=login_layout.findViewById(R.id.passwordtxt);

        alertDialog.setView(login_layout);
        alertDialog.setPositiveButton(activity.getResources().getString(R.string.login), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                //btnLogIn.setEnabled(false);
                if (TextUtils.isEmpty(etEmail.getText().toString())){
                    Snackbar.make(root, activity.getResources().getString(R.string.enter_email), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(etPassword.getText().toString())){
                    Snackbar.make(root, activity.getResources().getString(R.string.enter_password), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (etPassword.getText().toString().length()<6){
                    Snackbar.make(root, activity.getResources().getString(R.string.password_short), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                final SpotsDialog waitingDialog=new SpotsDialog(activity);
                waitingDialog.show();
                firebaseAuth.signInWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        waitingDialog.dismiss();
                        goToMainActivity();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Snackbar.make(root, activity.getResources().getString(R.string.failed)+e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        //btnLogIn.setEnabled(true);
                    }
                });
            }
        });
        alertDialog.setNegativeButton(activity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }
    public void showRegistrerDialog(){
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(activity);
        alertDialog.setTitle(activity.getResources().getString(R.string.signin));
        alertDialog.setMessage(activity.getResources().getString(R.string.fill_fields));

        LayoutInflater inflater=LayoutInflater.from(activity);
        View registrer_layout=inflater.inflate(R.layout.register_membership, null);
        final MaterialEditText etEmail=registrer_layout.findViewById(R.id.emailtxt);
        final MaterialEditText etPassword=registrer_layout.findViewById(R.id.passwordtxt);
        final MaterialEditText etName=registrer_layout.findViewById(R.id.nametxt);
        final MaterialEditText etPhone=registrer_layout.findViewById(R.id.phonetxt);

        alertDialog.setView(registrer_layout);
        alertDialog.setPositiveButton(activity.getResources().getString(R.string.register), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                if (TextUtils.isEmpty(etEmail.getText().toString())){
                    Snackbar.make(root, activity.getResources().getString(R.string.enter_email), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(etPassword.getText().toString())){
                    Snackbar.make(root, activity.getResources().getString(R.string.enter_password), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (etPassword.getText().toString().length()<6){
                    Snackbar.make(root, activity.getResources().getString(R.string.password_short), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(etName.getText().toString())){
                    Snackbar.make(root, activity.getResources().getString(R.string.enter_name), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(etPhone.getText().toString())){
                    Snackbar.make(root, activity.getResources().getString(R.string.enter_phone), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                firebaseAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                RouteDriver user=new RouteDriver();
                                user.setEmail(etEmail.getText().toString());
                                user.setName(etName.getText().toString());
                                user.setPassword(etPassword.getText().toString());
                                user.setPhone(etPhone.getText().toString());
                                user.setCarType("UberX");

                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(root, "Registered", Snackbar.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(root, activity.getResources().getString(R.string.failed)+e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(root, activity.getResources().getString(R.string.failed)+e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });

        alertDialog.setNegativeButton(activity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }
    public void showRegisterPhone(final RouteDriver user, final GoogleSignInAccount account){
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(activity);
        alertDialog.setTitle(activity.getResources().getString(R.string.signin));
        alertDialog.setMessage(activity.getResources().getString(R.string.fill_fields));

        LayoutInflater inflater=LayoutInflater.from(activity);
        View register_phone_layout=inflater.inflate(R.layout.layout_register_phone, null);
        final MaterialEditText etPhone=register_phone_layout.findViewById(R.id.etPhone);

        alertDialog.setView(register_phone_layout);
        alertDialog.setPositiveButton(activity.getResources().getString(R.string.login), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                user.setEmail(account.getEmail());
                user.setName(account.getDisplayName());
                user.setPassword(null);
                user.setPhone(etPhone.getText().toString());
                users.child(account.getId())
                        .setValue(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar.make(root, activity.getResources().getString(R.string.registered), Snackbar.LENGTH_SHORT).show();
                                loginSuccess();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(root, activity.getResources().getString(R.string.failed)+e.getMessage(), Snackbar.LENGTH_SHORT).show();

                    }
                });
            }
        });
        alertDialog.setNegativeButton(activity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }
    public void loginSuccess(){
        goToMainActivity();
    }
    private void goToMainActivity(){
        activity.startActivity(new Intent(activity, Driver_Home.class));
        activity.finish();
    }

    public void showDialogForgotPwd() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle("FORGOT PASSWORD");
        alertDialog.setMessage("Please enter your email address");
        LayoutInflater inflater = LayoutInflater.from(activity);
        View forgot_pwd_layout = inflater.inflate(R.layout.forgot_password_layout,null);
        final MaterialEditText edtEmail = (MaterialEditText)forgot_pwd_layout.findViewById(R.id.emailtxt);
        alertDialog.setView(forgot_pwd_layout);
        //set button
        alertDialog.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                final SpotsDialog waitingDialog = new SpotsDialog(activity);
                waitingDialog.show();
                firebaseAuth.sendPasswordResetEmail(edtEmail.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialogInterface.dismiss();
                                waitingDialog.dismiss();
                                Snackbar.make(root,"Reset password link has been sent",Snackbar.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialogInterface.dismiss();
                        waitingDialog.dismiss();
                        Snackbar.make(root,""+e.getMessage(),Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }
}
