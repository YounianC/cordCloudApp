package me.younian.cordcloud;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.leo.simplearcloader.ArcConfiguration;
import com.leo.simplearcloader.SimpleArcDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private String URL = "https://www.cordcloud.cc/";

    private String email = "";
    private String pass = "";
    public static int baseCount = 20;

    private String remain;
    private String used;
    private String today;


    private List<Map<String, String>> nodeList = new ArrayList<>();

    String cookieStr = "";
    @BindView(R.id.login)
    Button login;
    @BindView(R.id.nodes)
    ListView nodes;

    @BindView(R.id.email)
    EditText emailE;
    @BindView(R.id.pass)
    EditText passE;
    @BindView(R.id.baseCount)
    EditText baseCountE;

    @BindView(R.id.hongkong)
    CheckBox hongkong;
    @BindView(R.id.japan)
    CheckBox japan;
    @BindView(R.id.usa)
    CheckBox usa;

    @BindView(R.id.ifCheckIn)
    TextView ifCheckIn;

    private SimpleArcDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = emailE.getText().toString();
                pass = passE.getText().toString();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(baseCountE.getText().toString())) {
                    Toast.makeText(MainActivity.this, "请输入邮箱和密码！", Toast.LENGTH_LONG).show();
                    return;
                }
                baseCount = Integer.parseInt(baseCountE.getText().toString());
                SharedPreferencesHelper.putString(MainActivity.this, "email", email);
                SharedPreferencesHelper.putString(MainActivity.this, "pass", pass);
                SharedPreferencesHelper.putString(MainActivity.this, "baseCount", "" + baseCount);

                SharedPreferencesHelper.putString(MainActivity.this, "hongkong", "" + hongkong.isChecked());
                SharedPreferencesHelper.putString(MainActivity.this, "japan", "" + japan.isChecked());
                SharedPreferencesHelper.putString(MainActivity.this, "usa", "" + usa.isChecked());


                getUserInfo(false);
            }
        });

        login.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(MainActivity.this, "当前版本:" + getVersionName(MainActivity.this)
                        + "  by Younian", Toast.LENGTH_LONG).show();
                return false;
            }
        });

        ifCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryCheckIn();
            }
        });
    }

    private void init() {
        emailE.setText(SharedPreferencesHelper.getString(MainActivity.this, "email", ""));
        passE.setText(SharedPreferencesHelper.getString(MainActivity.this, "pass", ""));
        baseCountE.setText(SharedPreferencesHelper.getString(MainActivity.this, "baseCount", "20"));
        cookieStr = (SharedPreferencesHelper.getString(MainActivity.this, "cookieStr", ""));

        hongkong.setChecked(Boolean.parseBoolean(SharedPreferencesHelper.getString(MainActivity.this, "hongkong", "false")));
        japan.setChecked(Boolean.parseBoolean(SharedPreferencesHelper.getString(MainActivity.this, "japan", "false")));
        usa.setChecked(Boolean.parseBoolean(SharedPreferencesHelper.getString(MainActivity.this, "usa", "false")));

        getUserInfo(true);
    }

    private void login() {
        email = emailE.getText().toString();
        pass = passE.getText().toString();
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody phone = new FormBody.Builder()
                .add("email", email)
                .add("passwd", pass)
                .build();
        Request build = new Request.Builder()
                .url(URL + "auth/login").post(phone).build();
        Call call = okHttpClient.newCall(build);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                final String string = response.body().string();
                Log.e("RES", string);
                Gson gson = new Gson();
                final Map<String, String> map = gson.fromJson(string, new TypeToken<Map<String, String>>() {
                }.getType());
                if ("1".equals(map.get("ret"))) {
                    List<String> cookies = response.headers("set-cookie");
                    cookieStr = "";
                    for (String c : cookies) {
                        cookieStr += c.substring(0, c.indexOf(";") + 1);
                    }
                    Log.e("---", cookieStr);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "登陆成功", Toast.LENGTH_LONG).show();
                            SharedPreferencesHelper.putString(MainActivity.this, "cookieStr", cookieStr);
                            getUserInfo(true);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, map.get("msg"), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void tryCheckIn() {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        final Request original = chain.request();

                        final Request authorized = original.newBuilder()
                                .addHeader("Cookie", cookieStr)
                                .build();
                        return chain.proceed(authorized);
                    }
                })
                .build();

        RequestBody phone = new FormBody.Builder().build();
        Request build = new Request.Builder()
                .url(URL + "user/checkin").post(phone).build();
        Call call = okHttpClient.newCall(build);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final String string = response.body().string();
                if (!string.contains("DOCTYPE")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Gson gson = new Gson();
                            Map<String, String> map = gson.fromJson(string, new TypeToken<Map<String, String>>() {
                            }.getType());
                            if ("1".equals(map.get("ret"))) {
                                Toast.makeText(MainActivity.this, map.get("msg"), Toast.LENGTH_LONG).show();
                                if (map.get("msg").contains("您似乎已经续命过了")) {
                                    ifCheckIn.setText("已经续命");
                                } else {
                                    ifCheckIn.setText("未续命");
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void getUserInfo(final boolean start) {
        email = emailE.getText().toString();
        pass = passE.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(baseCountE.getText().toString())) {
            Toast.makeText(MainActivity.this, "请输入邮箱和密码！", Toast.LENGTH_LONG).show();
            return;
        }
        baseCount = Integer.parseInt(baseCountE.getText().toString());


        mDialog = new SimpleArcDialog(MainActivity.this);
        mDialog.setConfiguration(new ArcConfiguration(MainActivity.this));
        mDialog.show();

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        final Request original = chain.request();

                        final Request authorized = original.newBuilder()
                                .addHeader("Cookie", cookieStr)
                                .build();
                        return chain.proceed(authorized);
                    }
                })
                .build();

        Request build = new Request.Builder().url(URL + "user").get().build();
        Call call = okHttpClient.newCall(build);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                final String string = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (string.contains("未登录")) {
                            Toast.makeText(MainActivity.this, "登录失效，尝试登录", Toast.LENGTH_LONG).show();
                            login();
                            mDialog.dismiss();
                            return;
                        }

                        if (!string.contains("legendText:\"已用")) {
                            return;
                        }

                        used = string.substring(string.indexOf("legendText:\"已用") + 12, string.indexOf("\",", string.indexOf("legendText:\"已用")));
                        today = string.substring(string.indexOf("legendText:\"今日") + 12, string.indexOf("\",", string.indexOf("legendText:\"今日")));
                        remain = string.substring(string.indexOf("legendText:\"剩余") + 12, string.indexOf("\",", string.indexOf("legendText:\"剩余")));

                        getNodes();
                        if (start) {
                            tryCheckIn();
                        }
                    }
                });
            }
        });
    }

    private void getNodes() {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        final Request original = chain.request();

                        final Request authorized = original.newBuilder()
                                .addHeader("Cookie", cookieStr)
                                .build();
                        return chain.proceed(authorized);
                    }
                })
                .build();


        Request build = new Request.Builder().url(URL + "user/node").get().build();
        Call call = okHttpClient.newCall(build);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                final String string = response.body().string();

                runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void run() {
                        try {
                            if (string.contains("未登录")) {
                                Toast.makeText(MainActivity.this, "登录失效，尝试登录", Toast.LENGTH_LONG).show();
                                mDialog.dismiss();
                                return;
                            }

                            //加载一个Document对象。
                            Document doc = Jsoup.parse(string);
                            Elements elements = doc.select("div.tile-collapse");

                            nodeList.clear();

                            for (Element e : elements) {
                                String text = e.select("div.text-overflow").text();
                                String status = e.select("span").get(0).text();
                                String name = text.substring(0, text.indexOf("|"));
                                String person = text.substring(text.indexOf("person") + 6, text.indexOf("|", text.indexOf("person"))).trim();
                                String remain = text.substring(text.indexOf("traffic") + 7, text.indexOf("|", text.indexOf("traffic")));
                                String beilv = text.substring(text.lastIndexOf("|") + 9);

                                Map<String, String> map = new HashMap<>();
                                map.put("nodeName", name);
                                map.put("person", person);
                                map.put("remain", remain);
                                map.put("beilv", beilv);
                                map.put("status", status);

                                map.put("error", "0");
                                if (status.equals("warning") || Integer.parseInt(person) < baseCount) {
                                    map.put("error", "1");
                                }

                                //Filter
                                if (hongkong.isChecked() || japan.isChecked() || usa.isChecked()) {
                                    if (hongkong.isChecked() && (name.contains("港") || name.contains("台"))) {
                                        nodeList.add(map);
                                    }

                                    if (japan.isChecked() && name.contains("日本")) {
                                        nodeList.add(map);
                                    }

                                    if (usa.isChecked() && name.contains("美")) {
                                        nodeList.add(map);
                                    }
                                } else {
                                    nodeList.add(map);
                                }
                            }


                            nodeList.sort(new Comparator<Map<String, String>>() {
                                @Override
                                public int compare(Map<String, String> m1, Map<String, String> m2) {
                                    int flag = m1.get("error").compareTo(m2.get("error"));
                                    if (flag == 0) {
                                        return -(Integer.parseInt(m1.get("person")) - Integer.parseInt(m2.get("person")));
                                    } else {
                                        return flag;
                                    }
                                }
                            });

                            Map<String, String> map = new HashMap<>();
                            map.put("remain", remain);
                            map.put("used", used);
                            map.put("today", today);
                            nodeList.add(0, map);

                            NodeAdapter nodeAdapter = new NodeAdapter(MainActivity.this, nodeList);
                            nodes.setAdapter(nodeAdapter);
                            nodeAdapter.notifyDataSetChanged();
                            mDialog.dismiss();
                        } catch (Exception e) {
                            Log.e("mytag", e.toString());
                        }
                    }
                });
            }
        });
    }

    public String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String versionName = "";
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
}
