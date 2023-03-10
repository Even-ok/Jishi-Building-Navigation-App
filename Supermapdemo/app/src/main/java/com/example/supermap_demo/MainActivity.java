package com.example.supermap_demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import android.widget.ZoomControls;

import com.example.supermap_demo.expandtabview.SearchTabLeft;
import com.example.supermap_demo.expandtabview.SearchTabMiddle;
import com.example.supermap_demo.expandtabview.SearchTabRight;
import com.example.supermap_demo.expandtabview.SearchTabView;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;
import com.supermap.data.CursorType;
import com.supermap.data.DatasetVector;
import com.supermap.data.Datasets;
import com.supermap.data.Datasource;
import com.supermap.data.DatasourceConnectionInfo;
import com.supermap.data.Datasources;
import com.supermap.data.EngineType;
import com.supermap.data.Environment;
import com.supermap.data.GeoCoordSys;
import com.supermap.data.GeoCoordSysType;
import com.supermap.data.GeoSpatialRefType;
import com.supermap.data.Geometry;
import com.supermap.data.Point;
import com.supermap.data.Point2D;
import com.supermap.data.PrjCoordSys;
import com.supermap.data.PrjCoordSysType;
import com.supermap.data.QueryParameter;
import com.supermap.data.Recordset;
import com.supermap.data.Workspace;
import com.supermap.data.WorkspaceConnectionInfo;
import com.supermap.data.WorkspaceType;
import com.supermap.mapping.Action;
import com.supermap.mapping.CallOut;
import com.supermap.mapping.Layer;
import com.supermap.mapping.Layers;
import com.supermap.mapping.Map;
import com.supermap.mapping.MapControl;
import com.supermap.mapping.MapView;
import com.supermap.mapping.MeasureListener;
import com.supermap.mapping.Selection;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity{
    private ImageButton m_btnSelect;
    private Button m_btnInfo;
    private MapControl mapControl = null;
    private MapView m_mapView;
    private ZoomControls m_Zoom;
    private ImageButton btn_Close;
    private String mStrRoomNum;
    private String mStrRoomName;
    private String mStrRoomPerson;
    private String mStrRoomState;
    private TextView m_txtRoomNum;
    private TextView m_txtRoomName;
    private TextView m_txtRoomPerson;
    private TextView m_txtRoomState;
    private View m_DetailLayout;
    private PopupWindow pwDetailInfo;
    private PowerMenu profileMenu;
    private static final long DOUBLE_TIME = 500;
    private long lastClickTime = 0;


    RecyclerView rvSomething;
    //SwipeRefreshLayout swipeRefreshWidget;
    Button btnSearch;
    SearchTabView expandtabView;
    private ArrayList<View> mViewArray = new ArrayList<View>();
    private SearchTabLeft viewLeft;
    private SearchTabMiddle viewMiddle;
    private SearchTabRight viewRight;
    private String SearchContent;
    private List<String> mPics;


    private String longitude;
    private String latitude;
    private String districtcode;
    private String rank;
    private String specialty;

    private Dialog dialog;

    private String RoomNumber;
    private String RoomName;
    private String RoomPerson;
    private Integer NowAvailable;
    private String searchViewText;
    private EditText search_title;


    private final OnMenuItemClickListener<PowerMenuItem> onProfileItemClickListener =
            new OnMenuItemClickListener<PowerMenuItem>() {
                @Override
                public void onItemClick(int position, PowerMenuItem item) {
                    Toast.makeText(getBaseContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
                    profileMenu.dismiss();
                    String op = (String) item.getTitle();
                    if(op.equals("????????????"))
                    {
                        m_mapView.removeAllCallOut(); // ????????????Callout
                        mapControl.setAction(Action.PAN);
                        mapControl.getMap().refresh();
                    }
                    else if(op.equals("??????????????????")){
                        m_mapView.removeAllCallOut(); // ????????????Callout
                        mapControl.setAction(Action.SELECT);
                        QueryAll();
                        mapControl.getMap().refresh();
                    }
                    else if(op.equals("????????????")){
                        m_mapView.removeAllCallOut(); // ????????????Callout
                        mapControl.getMap().viewEntire();
                        mapControl.getMap().refresh();
                    }
                    else if(op.equals("????????????")){
                        m_mapView.removeAllCallOut(); // ????????????Callout
                        long currentTimeMillis = System.currentTimeMillis();
                        mapControl.setAction(Action.MEASURELENGTH);
                        lastClickTime = currentTimeMillis;
                        mapControl.getMap().refresh();
                    }
                    else
                        Toast.makeText(getBaseContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        /**
         * ?????????????????????
         */
        requestPermissions();
        Environment.setLicensePath("/sdcard/SuperMap/License/");
        Environment.setTemporaryPath("/sdcard/SuperMap/temp");
        Environment.setWebCacheDirectory("/sdcard/SuperMap/webCache");
        //CameraPermissionHelper.requestCameraPermission(this);

        //?????????????????????Environment???????????????????????????
        Environment.initialization(this);

        setContentView(R.layout.activity_main);

        initView();
        initData();
        initListener();

        profileMenu = PowerMenuUtils.getProfilePowerMenu(this, this, onProfileItemClickListener);


        //setContentView(R.layout.activity_main);

        //??????????????????
        Workspace m_workspace = new Workspace();
        WorkspaceConnectionInfo info = new WorkspaceConnectionInfo();
        info.setServer("/sdcard/SuperMap/GeometryInfo/indoor/supermapindoor.smwu");
        info.setType(WorkspaceType.SMWU);
        //info.setPassword("lyw278456123");
        m_workspace.open(info);

        //??????????????????????????????????????????
        m_mapView = findViewById(R.id.mapview);
        mapControl = m_mapView.getMapControl();
        mapControl.getMap().setWorkspace(m_workspace);

        //???????????????????????????5?????????
        String mapName = m_workspace.getMaps().get(4);
        Map mMap = mapControl.getMap();
        // ???????????????
        //PrjCoordSys prjCoordSys1 = new PrjCoordSys();
        //prjCoordSys1.fromXML("/sdcard/SuperMap/coordinate/Sphere Mercator.xml");
        //prjCoordSys1.setType(PrjCoordSysType.PCS_WGS_1984_UTM_15S);
        //mMap.setPrjCoordSys(prjCoordSys1);
        //mMap.fromXML("/sdcard/SuperMap/GeometryInfo/indoor.xml");
        mMap.setScale(0.002);


        mMap.open(mapName);
        mapControl.getMap().refresh();
        Layers layers = mMap.getLayers();
        System.out.println("layers:" + layers.getCount());
        //layers.contains("T7_CON_INFO@my_navi");
        Layer spaceLayer = layers.get("T7_REGION_INFO@my_navi#1");
        spaceLayer. setVisible (true);
        spaceLayer.setEditable(true);
        spaceLayer.setSelectable(true);

        mapControl.getMap().refresh();

        /**
         * ??????????????????
         */
//        m_btnSelect = (ImageButton)findViewById(R.id.btn_selectGeo);
//        m_btnSelect.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                mapControl.setAction(Action.SELECT);
//            }
//        });

        /**
         * ??????PopUp
         */
        LayoutInflater lfCallOut = getLayoutInflater();
        m_DetailLayout = lfCallOut.inflate(R.layout.detailinfo, null);

        pwDetailInfo = new PopupWindow(m_DetailLayout,380, WindowManager.LayoutParams.WRAP_CONTENT);
        m_txtRoomNum = (TextView)m_DetailLayout.findViewById(R.id.txt_country);
        m_txtRoomName = (TextView)m_DetailLayout.findViewById(R.id.txt_capital);
        m_txtRoomPerson = (TextView)m_DetailLayout.findViewById(R.id.txt_pop);
        m_txtRoomState = (TextView)m_DetailLayout.findViewById(R.id.txt_Continent);


        /**
         * ?????????????????????????????????
         */
//        m_btnSelect.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                QueryAll();
//            }
//        });


        /**
         * ??????????????????
         */
//        Button viewtire = (Button) findViewById(R.id.btn_viewentire);
//        final MapControl themapControl = mapControl;
//        final Map mmMap = mMap;
//        viewtire.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                themapControl.getMap().viewEntire();
//                themapControl.getMap().refresh();
//            }
//        });
        m_Zoom = (ZoomControls)findViewById(R.id.zoomControls1);
        m_Zoom.setOnZoomOutClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                mapControl.getMap().zoom(0.5);
                mapControl.getMap().refresh();
            }
        });
        m_Zoom.setOnZoomInClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                mapControl.getMap().zoom(2);
                mapControl.getMap().refresh();
            }
        });

        /**
         * ????????????
         */
//        Button measure = (Button) findViewById(R.id.btn_measure);
//        measure.setOnClickListener(new View.OnClickListener() {
//            private static final long DOUBLE_TIME = 500;
//            private long lastClickTime = 0;
//
//            @Override
//            public void onClick(View view) {
//                long currentTimeMillis = System.currentTimeMillis();
//                if (currentTimeMillis - lastClickTime < DOUBLE_TIME) {
//                    onDoubleClick(view);
//                }
//                else{
//                    themapControl.setAction(Action.MEASURELENGTH);
//                }
//                lastClickTime = currentTimeMillis;
//            }
//
//
//            public void onDoubleClick(View v) {
//                themapControl.getMap().getTrackingLayer().clear();
//                themapControl.setAction(Action.PAN);
//            }
//        });
        mapControl.addMeasureListener(new MeasureListener() {
            @Override
            public void lengthMeasured(double v, Point point) {
                Toast.makeText(MainActivity.this,"??????"+v+"???",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void areaMeasured(double v, Point point) {

            }

            @Override
            public void angleMeasured(double v, Point point) {

            }
        });

        mapControl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final Selection selection = spaceLayer.getSelection();
                if (selection == null) {
                    System.out.println("is null!!");
                }
                if (spaceLayer.getName() == null) {
                }
                if (selection.getCount() > 0) {
                    System.out.println("????????????");
                    return true;
                    // ????????????????????????????????????????????? ID
                    // ??????????????????
//                    queryInfoBubblePopupWindow.m_QueryInfoData.clear();
//                    queryInfoBubblePopupWindow.show(sceneControl, motionEvent.getX(), motionEvent.getY());
                }
                return false;
            }
        });


    }

    /**
     * ???????????????????????????
     */
    protected String[] needPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
    };

    /**
     * ??????????????????
     */
    public boolean checkPermissions(String[] permissions) {
        return EasyPermissions.hasPermissions(this, permissions);
    }
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        if (!checkPermissions(needPermissions)) {
            EasyPermissions.requestPermissions(
                    this,
                    "??????????????????????????????????????????????????????",
                    0,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE);
            //???????????????????????????????????????
        } else {
            //?????????????????????????????????
        }
    }
    // ?????????????????????????????????
    public void QueryAll(){
        // ?????????10???????????????????????????????????????????????????
        Layer layer = mapControl.getMap().getLayers().get(3);
        DatasetVector datasetvector = (DatasetVector)layer.getDataset();

        //??????????????????????????????
        Recordset recordset = datasetvector.getRecordset(false, CursorType.STATIC);

        if (recordset.getRecordCount()<1) {
            Toast.makeText(MainActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
            mapControl.getMap().refresh();
            return;
        }

        Point2D ptInner;
        recordset.moveFirst();
        Geometry geometry = recordset.getGeometry();

        m_mapView.removeAllCallOut(); // ????????????Callout

        while (!recordset.isEOF()) {
            geometry = recordset.getGeometry();
            ptInner = geometry.getInnerPoint();

            LayoutInflater lfCallOut = getLayoutInflater();
            View calloutLayout = lfCallOut.inflate(R.layout.layout_poi, null);

            TextView btnSelected = (TextView)calloutLayout.findViewById(R.id.tv_poiname);
            btnSelected.setText(recordset.getString("ROOMNUM"));
            btnSelected.setTag(geometry.getID());
            btnSelected.setOnClickListener(new detailClickListener());
            btnSelected.setTextColor(Color.WHITE);

            CallOut callout = new CallOut(MainActivity.this);
            callout.setContentView(calloutLayout);				// ??????????????????
            callout.setCustomize(true);							// ???????????????????????????
            callout.setLocation(ptInner.getX(), ptInner.getY());// ??????????????????
            m_mapView.addCallout(callout);

            recordset.moveNext();
        }

        m_mapView.showCallOut();								// ????????????
        mapControl.getMap().setCenter(geometry.getInnerPoint());
        mapControl.getMap().refresh();

        // ????????????
        recordset.dispose();
        geometry.dispose();
    }

    // ???????????????????????????
    public void QueryByRoomNumber(String RoomNumber){

        String strFilter = "ROOMNUM = '" + RoomNumber + "'";

        // ?????????10????????????????????????????????????????????????
        Layer layer = mapControl.getMap().getLayers().get(3);
        DatasetVector datasetvector = (DatasetVector)layer.getDataset();

        // ??????????????????
        QueryParameter parameter = new QueryParameter();
        parameter.setAttributeFilter(strFilter);
        parameter.setCursorType(CursorType.STATIC);

        Recordset recordset = datasetvector.query(parameter);

        if (recordset.getRecordCount()<1) {
            Toast.makeText(MainActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
            mapControl.getMap().refresh();
            return;
        }

        Point2D ptInner;
        recordset.moveFirst();
        Geometry geometry = recordset.getGeometry();

        m_mapView.removeAllCallOut(); // ????????????Callout

        while (!recordset.isEOF()) {
            geometry = recordset.getGeometry();
            ptInner = geometry.getInnerPoint();

            LayoutInflater lfCallOut = getLayoutInflater();
            View calloutLayout = lfCallOut.inflate(R.layout.layout_poi, null);

            TextView btnSelected = (TextView)calloutLayout.findViewById(R.id.tv_poiname);
            btnSelected.setText(recordset.getString("ROOMNUM"));
            btnSelected.setTag(geometry.getID());
            btnSelected.setOnClickListener(new detailClickListener());
            btnSelected.setTextColor(Color.WHITE);

            CallOut callout = new CallOut(MainActivity.this);
            callout.setContentView(calloutLayout);				// ??????????????????
            callout.setCustomize(true);							// ???????????????????????????
            callout.setLocation(ptInner.getX(), ptInner.getY());// ??????????????????
            m_mapView.addCallout(callout);

            recordset.moveNext();
        }

        m_mapView.showCallOut();								// ????????????
        mapControl.getMap().setCenter(geometry.getInnerPoint());
        mapControl.getMap().refresh();

        // ????????????
        recordset.dispose();
        geometry.dispose();
    }

    // ??????????????????????????????
    public void QueryByRoomName(String RoomName){

        String strFilter = "ROOMNAME = '" + RoomName + "'";

        // ?????????10????????????????????????????????????????????????
        Layer layer = mapControl.getMap().getLayers().get(3);
        DatasetVector datasetvector = (DatasetVector)layer.getDataset();

        // ??????????????????
        QueryParameter parameter = new QueryParameter();
        parameter.setAttributeFilter(strFilter);
        parameter.setCursorType(CursorType.STATIC);

        Recordset recordset = datasetvector.query(parameter);

        if (recordset.getRecordCount()<1) {
            Toast.makeText(MainActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
            mapControl.getMap().refresh();
            return;
        }

        Point2D ptInner;
        recordset.moveFirst();
        Geometry geometry = recordset.getGeometry();

        m_mapView.removeAllCallOut(); // ????????????Callout

        while (!recordset.isEOF()) {
            geometry = recordset.getGeometry();
            ptInner = geometry.getInnerPoint();

            LayoutInflater lfCallOut = getLayoutInflater();
            View calloutLayout = lfCallOut.inflate(R.layout.layout_poi, null);

            TextView btnSelected = (TextView)calloutLayout.findViewById(R.id.tv_poiname);
            btnSelected.setText(recordset.getString("ROOMNUM"));
            btnSelected.setTag(geometry.getID());
            btnSelected.setOnClickListener(new detailClickListener());
            btnSelected.setTextColor(Color.WHITE);

            CallOut callout = new CallOut(MainActivity.this);
            callout.setContentView(calloutLayout);				// ??????????????????
            callout.setCustomize(true);							// ???????????????????????????
            callout.setLocation(ptInner.getX(), ptInner.getY());// ??????????????????
            m_mapView.addCallout(callout);

            recordset.moveNext();
        }

        m_mapView.showCallOut();								// ????????????
        mapControl.getMap().setCenter(geometry.getInnerPoint());
        mapControl.getMap().refresh();

        // ????????????
        recordset.dispose();
        geometry.dispose();
    }

    // ??????????????????????????????
    public void QueryByRoomPerson(String RoomPerson){

        String strFilter = "ROOMPERSON = '" + RoomPerson + "'";

        // ?????????10????????????????????????????????????????????????
        Layer layer = mapControl.getMap().getLayers().get(3);
        DatasetVector datasetvector = (DatasetVector)layer.getDataset();

        // ??????????????????
        QueryParameter parameter = new QueryParameter();
        parameter.setAttributeFilter(strFilter);
        parameter.setCursorType(CursorType.STATIC);

        Recordset recordset = datasetvector.query(parameter);

        if (recordset.getRecordCount()<1) {
            Toast.makeText(MainActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
            mapControl.getMap().refresh();
            return;
        }

        Point2D ptInner;
        recordset.moveFirst();
        Geometry geometry = recordset.getGeometry();

        m_mapView.removeAllCallOut(); // ????????????Callout

        while (!recordset.isEOF()) {
            geometry = recordset.getGeometry();
            ptInner = geometry.getInnerPoint();

            LayoutInflater lfCallOut = getLayoutInflater();
            View calloutLayout = lfCallOut.inflate(R.layout.layout_poi, null);

            TextView btnSelected = (TextView)calloutLayout.findViewById(R.id.tv_poiname);
            btnSelected.setText(recordset.getString("ROOMNUM"));
            btnSelected.setTag(geometry.getID());
            btnSelected.setOnClickListener(new detailClickListener());
            btnSelected.setTextColor(Color.WHITE);

            CallOut callout = new CallOut(MainActivity.this);
            callout.setContentView(calloutLayout);				// ??????????????????
            callout.setCustomize(true);							// ???????????????????????????
            callout.setLocation(ptInner.getX(), ptInner.getY());// ??????????????????
            m_mapView.addCallout(callout);

            recordset.moveNext();
        }

        m_mapView.showCallOut();								// ????????????
        mapControl.getMap().setCenter(geometry.getInnerPoint());
        mapControl.getMap().refresh();

        // ????????????
        recordset.dispose();
        geometry.dispose();
    }


    class detailClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (btn_Close != null)
                btn_Close.performClick();

            String strID = v.getTag().toString();
            System.out.println(strID);
            QuerybyID(strID);


            m_txtRoomNum.setText(mStrRoomNum);
            m_txtRoomName.setText(mStrRoomName);
            m_txtRoomPerson.setText(mStrRoomPerson);
            m_txtRoomState.setText(mStrRoomState);

//			pwDetailInfo.showAtLocation(m_mapControl, Gravity.NO_GRAVITY, 8, 86);
            pwDetailInfo.showAsDropDown(v, 60, -60);
            btn_Close = (ImageButton)m_DetailLayout.findViewById(R.id.btn_close);

            btn_Close.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    pwDetailInfo.dismiss();
                }
            });
        }
    }

    // ID??????
    private void QuerybyID(String id){
        String strFilter = "SMID = '" + id + "'";

        // ?????????10????????????????????????????????????????????????
        Layer layer = mapControl.getMap().getLayers().get(3);
        DatasetVector datasetvector = (DatasetVector)layer.getDataset();

        // ??????????????????
        QueryParameter parameter = new QueryParameter();
        parameter.setAttributeFilter(strFilter);
        parameter.setCursorType(CursorType.STATIC);

        // ????????????????????????????????????
        Recordset recordset = datasetvector.query(parameter);

        if (recordset.getRecordCount()<1) {
            return;
        }

        recordset.moveFirst();

        mStrRoomNum = recordset.getFieldValue("ROOMNUM").toString();
        mStrRoomName = recordset.getFieldValue("ROOMNAME").toString();
        mStrRoomState = recordset.getFieldValue("ROOMSTATE").toString();
        mStrRoomPerson = recordset.getFieldValue("ROOMPERSON").toString();

        // ????????????
        recordset.dispose();
    }
    public void onProfile(View view) {
        profileMenu.showAsDropDown(view, -370, 0);
    }


    private void initView() {

        viewLeft = new SearchTabLeft(this);
        viewMiddle = new SearchTabMiddle(this);
        viewRight = new SearchTabRight(this);
        //swipeRefreshWidget = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_widget);
        rvSomething = (RecyclerView) findViewById(android.R.id.list);
        expandtabView = (SearchTabView) findViewById(R.id.expandtab_view);
        btnSearch = (Button) findViewById(R.id.btn_search);
        search_title = (EditText) findViewById(R.id.search_title);

//        swipeRefreshWidget.setOnRefreshListener(new androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                swipeRefreshWidget.setRefreshing(true);
//                Log.d("Swipe", "Refreshing Number");
//                (new Handler()).postDelayed(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        swipeRefreshWidget.setRefreshing(false);
//                        Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_SHORT).show();
//                    }
//                }, 3000);
//            }
//        });
    }

        private void initListener() {
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (NowAvailable == 0)
                    {
                        Toast.makeText(MainActivity.this,"?????????????????????????????????", Toast.LENGTH_SHORT).show();
                    }
                    else if (NowAvailable == 1){    //????????????
                        if(RoomNumber.equals("??????")){
                            searchViewText = search_title.getText().toString();
                            QueryByRoomNumber(searchViewText);
                        }
                        else
                        QueryByRoomNumber(RoomNumber);
                    }
                    else if(NowAvailable == 2)    //????????????
                    {
                        if(RoomName.equals("??????")){
                            searchViewText = search_title.getText().toString();
                            QueryByRoomName(searchViewText);
                        }
                        else
                        QueryByRoomName(RoomName);
                    }
                    else{    //?????????
                        if(RoomPerson.equals("??????")){
                            searchViewText = search_title.getText().toString();
                            QueryByRoomPerson(searchViewText);
                        }
                        else
                        QueryByRoomPerson(RoomPerson);
                    }
                }
            });

            viewLeft.setOnSelectListener(new SearchTabLeft.OnSelectListener() {

                @Override
                public void getValue(String itemCode, String showText) {

                        districtcode = itemCode;
                        onRefresh(viewLeft, showText);
                        if(!showText.equals("????????????")) {
                        RoomNumber = showText;
                        NowAvailable = 1;
                    }

                }
            });

            viewMiddle.setOnSelectListener(new SearchTabMiddle.OnSelectListener() {

                @Override
                public void getValue(String itemLvTwocode, String showText) {
                        rank = itemLvTwocode;
                        onRefresh(viewMiddle, showText);
                        if(!showText.equals("????????????")) {
                        RoomName = showText;
                        NowAvailable = 2;
                    }

                }
            });

            viewRight.setOnSelectListener(new SearchTabRight.OnSelectListener() {

                @Override
                public void getValue(String itemCode, String showText) {
                        specialty = itemCode;
                        onRefresh(viewRight, showText);
                        if(!showText.equals("????????????")) {
                        RoomPerson = showText;
                        NowAvailable = 3;
                    }
                }
            });

        }


        private void onRefresh(View view, String showText) {

            expandtabView.onPressBack();
            int position = getPositon(view);
            if (position >= 0 && !expandtabView.getTitle(position).equals(showText)) {
                expandtabView.setTitle(showText, position);
            }
            Toast.makeText(MainActivity.this, showText, Toast.LENGTH_SHORT).show();


        }

        private int getPositon(View tView) {
            for (int i = 0; i < mViewArray.size(); i++) {
                if (mViewArray.get(i) == tView) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public void onBackPressed() {

            if (!expandtabView.onPressBack()) {
                finish();
            }

        }

    private void initData() {

        NowAvailable = 0;
        RoomNumber = null;
        RoomName = null;
        RoomPerson =  null;
        // tab??????
        mViewArray.add(viewLeft);
        mViewArray.add(viewMiddle);
        mViewArray.add(viewRight);

        mPics=new ArrayList<String>();
        for (int i = 0; i < 15; i++) {
            if (i == 2 || i == 4 || i == 5 || i == 8 || i == 10) {
                mPics.add("");
            } else {
                mPics.add("https://www.eff.org/sites/default/files/chrome150_0.jpg");
            }
        }
        //mStaggeredAdapter = new StaggeredAdapter(this, mPics);
//            rvSomething.setLayoutManager(new StaggeredGridLayoutManager(2,
//                    StaggeredGridLayoutManager.VERTICAL));
//            rvSomething.setAdapter(mStaggeredAdapter);
//            // ??????item??????
//            rvSomething.setItemAnimator(new DefaultItemAnimator());
//            rvSomething.addItemDecoration(new DividerItemDecoration(this,
//                    DividerItemDecoration.VERTICAL_LIST));
//            rvSomething.addItemDecoration(new DividerItemDecoration(this,
//                    DividerItemDecoration.HORIZONTAL_LIST));
        //initEvent();

        ArrayList<String> mTextArray = new ArrayList<String>();
        mTextArray.add(getResources().getString(R.string.region_serach_title));
        mTextArray.add(getResources().getString(R.string.rank_serach_title));
        mTextArray.add(getResources().getString(R.string.specialty_serach_title));
        expandtabView.setValue(mTextArray, mViewArray);
        expandtabView.setTitle(mTextArray.get(0), 0);
        expandtabView.setTitle(mTextArray.get(1), 1);
        expandtabView.setTitle(mTextArray.get(2), 2);

    }

    }
