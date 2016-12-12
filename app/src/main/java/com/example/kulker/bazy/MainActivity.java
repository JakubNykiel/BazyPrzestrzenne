package com.example.kulker.bazy;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geodatabase.ShapefileFeatureTable;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileNotFoundException;

import static android.R.attr.y;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase database;
    RelativeLayout main;
    MapView mapView;
    Button start;
    ShapefileFeatureTable shapefileFeatureTable;
    FeatureLayer featureLayer;
    GraphicsLayer graphicsLayer = new GraphicsLayer();
    TextView pointData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = FirebaseDatabase.getInstance();
        main = (RelativeLayout)findViewById(R.id.activity_main);
        mapView = (MapView)main.findViewById(R.id.map);
        pointData = (TextView)main.findViewById(R.id.pointData);

        LoadFile();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartButtonListnerMethod();
            }
        });

        featureLayer = new FeatureLayer(shapefileFeatureTable);
        featureLayer.setRenderer(new SimpleRenderer(new SimpleFillSymbol(
                getResources().getColor(android.R.color.holo_blue_bright),
                SimpleFillSymbol.STYLE.SOLID)));

        mapView.addLayer(featureLayer);
        mapView.addLayer(graphicsLayer);
    }

    private void StartButtonListnerMethod(){
        mapView.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(float v, float v1) {
                AddMarkerToMap(v,v1);
            }
        });
    }

    private void AddMarkerToMap(float v, float v1){
        SimpleMarkerSymbol simpleMarker = new SimpleMarkerSymbol(Color.RED, 10, SimpleMarkerSymbol.STYLE.CIRCLE);

        Point pointGeometry = mapView.toMapPoint(v,v1);
        Graphic pointGraphic = new Graphic(pointGeometry, simpleMarker);
        graphicsLayer.addGraphic(pointGraphic);
        PringPointData(new Point(v,v1));
    }

    private void LoadFile(){
        String shapeFilePath = Environment.getExternalStorageDirectory() + "/budynki.shp";
        start = (Button)main.findViewById(R.id.startBtn);
        try {
            File shapefile = new File(shapeFilePath);
            shapefileFeatureTable = new ShapefileFeatureTable(shapefile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            Toast.makeText(this, "ShapeFile Not Found!!!", Toast.LENGTH_LONG).show();
            ex.printStackTrace();
            return;
        }
    }

    private void PringPointData(Point point){

        Point p = ConvertPoint(point);
        String tmp = "X: "+p.getX()+", Y: "+p.getY();
        pointData.setText(tmp);
    }

    private Point ConvertPoint(Point point){

        SpatialReference srFrom = mapView.getSpatialReference();
        SpatialReference srTo = SpatialReference.create(3857); //WGS84
        return (Point) GeometryEngine.project(point, srTo, srFrom);
        //return (Point) GeometryEngine.project(point.getX(), point.getY(),srFrom);
    }
}

