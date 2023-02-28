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
                    if(op.equals("移动地图"))
                    {
                        m_mapView.removeAllCallOut(); // 移除所有Callout
                        mapControl.setAction(Action.PAN);
                        mapControl.getMap().refresh();
                    }
                    else if(op.equals("查看房间信息")){
                        m_mapView.removeAllCallOut(); // 移除所有Callout
                        mapControl.setAction(Action.SELECT);
                        QueryAll();
                        mapControl.getMap().refresh();
                    }
                    else if(op.equals("全屏显示")){
                        m_mapView.removeAllCallOut(); // 移除所有Callout
                        mapControl.getMap().viewEntire();
                        mapControl.getMap().refresh();
                    }
                    else if(op.equals("距离测算")){
                        m_mapView.removeAllCallOut(); // 移除所有Callout
                        long currentTimeMillis = System.currentTimeMillis();
                        mapControl.setAction(Action.MEASURELENGTH);
                        lastClickTime = currentTimeMillis;
                        mapControl.getMap().refresh();
                    }
                    else
                        Toast.makeText(getBaseContext(), "没有对应操作", Toast.LENGTH_SHORT).show();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        /**
         * 初始化工作环境
         */
        requestPermissions();
        Environment.setLicensePath("/sdcard/SuperMap/License/");
        Environment.setTemporaryPath("/sdcard/SuperMap/temp");
        Environment.setWebCacheDirectory("/sdcard/SuperMap/webCache");
        //CameraPermissionHelper.requestCameraPermission(this);

        //组件功能必须在Environment初始化之后才能调用
        Environment.initialization(this);

        setContentView(R.layout.activity_main);

        initView();
        initData();
        initListener();

        profileMenu = PowerMenuUtils.getProfilePowerMenu(this, this, onProfileItemClickListener);


        //setContentView(R.layout.activity_main);

        //打开工作空间
        Workspace m_workspace = new Workspace();
        WorkspaceConnectionInfo info = new WorkspaceConnectionInfo();
        info.setServer("/sdcard/SuperMap/GeometryInfo/indoor/supermapindoor.smwu");
        info.setType(WorkspaceType.SMWU);
        //info.setPassword("lyw278456123");
        m_workspace.open(info);

        //将地图显示控件和工作空间关联
        m_mapView = findViewById(R.id.mapview);
        mapControl = m_mapView.getMapControl();
        mapControl.getMap().setWorkspace(m_workspace);

        //打开工作空间中的第5幅地图
        String mapName = m_workspace.getMaps().get(4);
        Map mMap = mapControl.getMap();
        // 修改坐标系
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
         * 绑定选择按钮
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
         * 绑定PopUp
         */
        LayoutInflater lfCallOut = getLayoutInflater();
        m_DetailLayout = lfCallOut.inflate(R.layout.detailinfo, null);

        pwDetailInfo = new PopupWindow(m_DetailLayout,380, WindowManager.LayoutParams.WRAP_CONTENT);
        m_txtRoomNum = (TextView)m_DetailLayout.findViewById(R.id.txt_country);
        m_txtRoomName = (TextView)m_DetailLayout.findViewById(R.id.txt_capital);
        m_txtRoomPerson = (TextView)m_DetailLayout.findViewById(R.id.txt_pop);
        m_txtRoomState = (TextView)m_DetailLayout.findViewById(R.id.txt_Continent);


        /**
         * 显示地图上所有位置信息
         */
//        m_btnSelect.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                QueryAll();
//            }
//        });


        /**
         * 查看全景地图
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
         * 距离量算
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
                Toast.makeText(MainActivity.this,"距离"+v+"米",Toast.LENGTH_SHORT).show();
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
                    System.out.println("选中啦！");
                    return true;
                    // 返回选择集中指定几何对象的系统 ID
                    // 本地数据获取
//                    queryInfoBubblePopupWindow.m_QueryInfoData.clear();
//                    queryInfoBubblePopupWindow.show(sceneControl, motionEvent.getX(), motionEvent.getY());
                }
                return false;
            }
        });


    }

    /**
     * 需要申请的权限数组
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
     * 申请动态权限
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
                    "为了应用的正常使用，请允许以下权限。",
                    0,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE);
            //没有授权，编写申请权限代码
        } else {
            //已经授权，执行操作代码
        }
    }
    // 初始化控件，绑定监听器
    public void QueryAll(){
        // 获得第10个图层，现在还不知道是哪个图层。。
        Layer layer = mapControl.getMap().getLayers().get(3);
        DatasetVector datasetvector = (DatasetVector)layer.getDataset();

        //获取所有记录的记录机
        Recordset recordset = datasetvector.getRecordset(false, CursorType.STATIC);

        if (recordset.getRecordCount()<1) {
            Toast.makeText(MainActivity.this, "未搜索到对象", Toast.LENGTH_SHORT).show();
            mapControl.getMap().refresh();
            return;
        }

        Point2D ptInner;
        recordset.moveFirst();
        Geometry geometry = recordset.getGeometry();

        m_mapView.removeAllCallOut(); // 移除所有Callout

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
            callout.setContentView(calloutLayout);				// 设置显示内容
            callout.setCustomize(true);							// 设置自定义背景图片
            callout.setLocation(ptInner.getX(), ptInner.getY());// 设置显示位置
            m_mapView.addCallout(callout);

            recordset.moveNext();
        }

        m_mapView.showCallOut();								// 显示标注
        mapControl.getMap().setCenter(geometry.getInnerPoint());
        mapControl.getMap().refresh();

        // 释放资源
        recordset.dispose();
        geometry.dispose();
    }

    // 根据房间号获取地址
    public void QueryByRoomNumber(String RoomNumber){

        String strFilter = "ROOMNUM = '" + RoomNumber + "'";

        // 获得第10个图层，现在还不知道是第几个拉！
        Layer layer = mapControl.getMap().getLayers().get(3);
        DatasetVector datasetvector = (DatasetVector)layer.getDataset();

        // 设置查询参数
        QueryParameter parameter = new QueryParameter();
        parameter.setAttributeFilter(strFilter);
        parameter.setCursorType(CursorType.STATIC);

        Recordset recordset = datasetvector.query(parameter);

        if (recordset.getRecordCount()<1) {
            Toast.makeText(MainActivity.this, "未搜索到对象", Toast.LENGTH_SHORT).show();
            mapControl.getMap().refresh();
            return;
        }

        Point2D ptInner;
        recordset.moveFirst();
        Geometry geometry = recordset.getGeometry();

        m_mapView.removeAllCallOut(); // 移除所有Callout

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
            callout.setContentView(calloutLayout);				// 设置显示内容
            callout.setCustomize(true);							// 设置自定义背景图片
            callout.setLocation(ptInner.getX(), ptInner.getY());// 设置显示位置
            m_mapView.addCallout(callout);

            recordset.moveNext();
        }

        m_mapView.showCallOut();								// 显示标注
        mapControl.getMap().setCenter(geometry.getInnerPoint());
        mapControl.getMap().refresh();

        // 释放资源
        recordset.dispose();
        geometry.dispose();
    }

    // 根据房间名字获取地址
    public void QueryByRoomName(String RoomName){

        String strFilter = "ROOMNAME = '" + RoomName + "'";

        // 获得第10个图层，现在还不知道是第几个拉！
        Layer layer = mapControl.getMap().getLayers().get(3);
        DatasetVector datasetvector = (DatasetVector)layer.getDataset();

        // 设置查询参数
        QueryParameter parameter = new QueryParameter();
        parameter.setAttributeFilter(strFilter);
        parameter.setCursorType(CursorType.STATIC);

        Recordset recordset = datasetvector.query(parameter);

        if (recordset.getRecordCount()<1) {
            Toast.makeText(MainActivity.this, "未搜索到对象", Toast.LENGTH_SHORT).show();
            mapControl.getMap().refresh();
            return;
        }

        Point2D ptInner;
        recordset.moveFirst();
        Geometry geometry = recordset.getGeometry();

        m_mapView.removeAllCallOut(); // 移除所有Callout

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
            callout.setContentView(calloutLayout);				// 设置显示内容
            callout.setCustomize(true);							// 设置自定义背景图片
            callout.setLocation(ptInner.getX(), ptInner.getY());// 设置显示位置
            m_mapView.addCallout(callout);

            recordset.moveNext();
        }

        m_mapView.showCallOut();								// 显示标注
        mapControl.getMap().setCenter(geometry.getInnerPoint());
        mapControl.getMap().refresh();

        // 释放资源
        recordset.dispose();
        geometry.dispose();
    }

    // 根据房间名字获取地址
    public void QueryByRoomPerson(String RoomPerson){

        String strFilter = "ROOMPERSON = '" + RoomPerson + "'";

        // 获得第10个图层，现在还不知道是第几个拉！
        Layer layer = mapControl.getMap().getLayers().get(3);
        DatasetVector datasetvector = (DatasetVector)layer.getDataset();

        // 设置查询参数
        QueryParameter parameter = new QueryParameter();
        parameter.setAttributeFilter(strFilter);
        parameter.setCursorType(CursorType.STATIC);

        Recordset recordset = datasetvector.query(parameter);

        if (recordset.getRecordCount()<1) {
            Toast.makeText(MainActivity.this, "未搜索到对象", Toast.LENGTH_SHORT).show();
            mapControl.getMap().refresh();
            return;
        }

        Point2D ptInner;
        recordset.moveFirst();
        Geometry geometry = recordset.getGeometry();

        m_mapView.removeAllCallOut(); // 移除所有Callout

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
            callout.setContentView(calloutLayout);				// 设置显示内容
            callout.setCustomize(true);							// 设置自定义背景图片
            callout.setLocation(ptInner.getX(), ptInner.getY());// 设置显示位置
            m_mapView.addCallout(callout);

            recordset.moveNext();
        }

        m_mapView.showCallOut();								// 显示标注
        mapControl.getMap().setCenter(geometry.getInnerPoint());
        mapControl.getMap().refresh();

        // 释放资源
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

    // ID查询
    private void QuerybyID(String id){
        String strFilter = "SMID = '" + id + "'";

        // 获得第10个图层，现在还不知道是第几个拉！
        Layer layer = mapControl.getMap().getLayers().get(3);
        DatasetVector datasetvector = (DatasetVector)layer.getDataset();

        // 设置查询参数
        QueryParameter parameter = new QueryParameter();
        parameter.setAttributeFilter(strFilter);
        parameter.setCursorType(CursorType.STATIC);

        // 查询，返回查询结果记录集
        Recordset recordset = datasetvector.query(parameter);

        if (recordset.getRecordCount()<1) {
            return;
        }

        recordset.moveFirst();

        mStrRoomNum = recordset.getFieldValue("ROOMNUM").toString();
        mStrRoomName = recordset.getFieldValue("ROOMNAME").toString();
        mStrRoomState = recordset.getFieldValue("ROOMSTATE").toString();
        mStrRoomPerson = recordset.getFieldValue("ROOMPERSON").toString();

        // 释放资源
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
//                        Toast.makeText(MainActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainActivity.this,"您必须先选择一个选项！", Toast.LENGTH_SHORT).show();
                    }
                    else if (NowAvailable == 1){    //按房间号
                        if(RoomNumber.equals("其他")){
                            searchViewText = search_title.getText().toString();
                            QueryByRoomNumber(searchViewText);
                        }
                        else
                        QueryByRoomNumber(RoomNumber);
                    }
                    else if(NowAvailable == 2)    //按房间名
                    {
                        if(RoomName.equals("其他")){
                            searchViewText = search_title.getText().toString();
                            QueryByRoomName(searchViewText);
                        }
                        else
                        QueryByRoomName(RoomName);
                    }
                    else{    //按人员
                        if(RoomPerson.equals("其他")){
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
                        if(!showText.equals("全部房号")) {
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
                        if(!showText.equals("全部房名")) {
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
                        if(!showText.equals("全部人员")) {
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
        // tab设置
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
//            // 设置item动画
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
