package com.rodpil.rubik;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ImageManipulationsActivity extends Activity implements CvCameraViewListener2 {
    private static final String  TAG                 = "OCVSample::Activity";
    public static final int PICK_IMAGE = 1;
    private String[] COLORS = EColors.colors();
    public static final int      VIEW_MODE_RGBA      = 0;
    public static final int      VIEW_MODE_HIST      = 1;
    public static final int      VIEW_MODE_CANNY     = 2;
    public static final int      VIEW_MODE_SEPIA     = 3;
    public static final int      VIEW_MODE_SOBEL     = 4;
    public static final int      VIEW_MODE_ZOOM      = 5;
    public static final int      VIEW_MODE_PIXELIZE  = 6;
    public static final int      VIEW_MODE_POSTERIZE = 7;

    private MenuItem             mItemPreviewRGBA;
    private MenuItem             mItemPreviewHist;
    private MenuItem             mItemPreviewCanny;
    private MenuItem             mItemPreviewSepia;
    private MenuItem             mItemPreviewSobel;
    private MenuItem             mItemPreviewZoom;
    private MenuItem             mItemPreviewPixelize;
    private MenuItem             mItemPreviewPosterize;
    private CameraBridgeViewBase mOpenCvCameraView;

    private Size                 mSize0;

    private Mat                  mIntermediateMat;
    private Mat                  mMat0;
    private MatOfInt             mChannels[];
    private MatOfInt             mHistSize;
    private int                  mHistSizeNum = 25;
    private MatOfFloat           mRanges;
    private Scalar               mColorsRGB[];
    private Scalar               mColorsHue[];
    private Scalar               mWhilte;
    private Point                mP1;
    private Point                mP2;
    private float                mBuff[];
    private Mat                  mSepiaKernel;
    Button[] btn = new Button[9];
    private Button btnScan;
    List<Cubie> alCubie = new ArrayList<>();
    private Integer[][] faces = new Integer[6][];
    private Spinner dropdown;
    EditText editTextAddress, editTextSecret;
    private Button btnCo;
    public static int           viewMode = VIEW_MODE_RGBA;
    TextView textResponse;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    public ImageManipulationsActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        editTextAddress = (EditText)findViewById(R.id.address);
        Button b = (Button) findViewById(R.id.connect);
        b.setOnClickListener(buttonConnectOnClickListener);


        editTextSecret = (EditText)findViewById(R.id.port);
        editTextSecret.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String secret = s.toString();
                if (secret.toLowerCase().equals("flamingo"))
                    Toast.makeText
                            (getApplicationContext(), "Execution ! " + ("\ud83d\ude01"), Toast.LENGTH_SHORT)
                            .show();
                executeTestFlamingo();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        textResponse = (TextView)findViewById(R.id.response);

        dropdown = findViewById(R.id.spinner1);
//create a list of items for the spinner.
        String[] items = new String[]{"Face 1", "Face 2", "Face 3","Face 4", "Face 5", "Face 6"};
//create an adapter to describe how the items are displayed, adapters are used in several places in android.
//There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
//set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);

                if (faces[position] != null)
                {
                    Integer[] array = faces[position];
                    int index = 0;
                    for (Button b : btn) {
                        b.setBackgroundColor(Color.parseColor(EColors.retrieveByBind(array[index]).getHex()));
                        index++;
                    }
                }
                else
                    for (Button b: btn)
                        b.setBackgroundColor(Color.parseColor("#ffffff"));

                // Notify the selected item text
                Toast.makeText
                        (getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        GridLayout gl = (GridLayout) findViewById(R.id.grid);

        for (int i = 0 ; i<9; i++)
        {
            Button btnTmp = new Button(this);
            btnTmp.setText("Cube N°" + i);
            btnTmp.setBackgroundColor(Color.parseColor("#ffffff"));
            btnTmp.setOnClickListener(new ButtonClicker(i));

            btnTmp.setWidth(0);
            btnTmp.setHeight(100);

            btn[i] = btnTmp;

            gl.addView(btnTmp);
        }

        btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        Button btnValidate = (Button) findViewById(R.id.btnValider);
        btnCo = (Button) findViewById(R.id.btnConnect);
        btnCo.setVisibility(View.GONE);

        btnValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer[] array = new Integer[9];
                int i = 0;
                for (Button b: btn) {
                    int color = ((ColorDrawable)b.getBackground()).getColor();
                    String hex = Integer.toHexString( color ).substring(2,Integer.toHexString( color ).length());
                    Log.d("COLOR", hex);
                    EColors c = EColors.retrieveByCoul(hex);
                    Log.d("COLOR", String.valueOf(c.getBind()));
                    //EColors e = EColors.getClosest(color);
                    array[i] = c.getBind();
                    i++;
                }
                Log.d("DROP", String.valueOf(dropdown.getSelectedItemPosition()));
                faces[dropdown.getSelectedItemPosition()] = array;
                boolean error = false;
                for (Integer[] face: faces) {
                    if (face==null)
                        error = true;
                }
                if (!error)
                    btnCo.setVisibility(View.VISIBLE);

            }
        });

        btnCo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Integer> list = new ArrayList<>();
                Boolean error = false;
                Map<Integer, Integer> counter = new HashMap<>();

                for (Integer[] face : faces) {
                    for (Integer value : face) {
                        if (!counter.containsKey(value))
                            counter.put(value, 1);
                        else
                            counter.computeIfPresent(value, (k, val) -> val + 1);
                    }
                    Log.d("FACES", Arrays.toString(face));
                }

                counter.forEach((k,val) -> {
                    String msg = (val != 9) ? "Face "+ (k+1) +" ratée" : "Face "+ (k+1) + " validée !";
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                });

                Client myClientTask = new Client(
                        editTextAddress.getText().toString(),2983, faces, editTextSecret);
                myClientTask.execute();

            }


        });



        /*Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
*/
    }


    View.OnClickListener buttonConnectOnClickListener =
            new View.OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    MyClientTask myClientTask = new MyClientTask(
                            editTextAddress.getText().toString(),
                            2983);
                    myClientTask.execute();
                }};

    private void executeTestFlamingo() {
        this.faces = TestCube.flamingo();
        this.btnCo.setVisibility(View.VISIBLE);
    }

    public void onClickBtn(int btnIndex, int index)
    {
        btn[btnIndex].setBackgroundColor(Color.parseColor(COLORS[index]));

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE) {
            Uri uri = data.getData();

            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                Mat srcMat = new Mat ( bmp.getHeight(), bmp.getWidth(), CvType.CV_8U);

                Bitmap myBitmap32 = bmp.copy(Bitmap.Config.ARGB_8888, true);

                Utils.bitmapToMat(myBitmap32, srcMat);

                srcMat = findLargestRectangle(srcMat, bmp);


                /*Mat gray = new Mat(srcMat.size(), CvType.CV_8UC1);
                Imgproc.cvtColor(srcMat, gray, Imgproc.COLOR_RGB2GRAY);
                Mat edge = new Mat();
                Mat dst = new Mat();
                Imgproc.Canny(gray, edge, 80, 90);
                Imgproc.cvtColor(edge, dst, Imgproc.COLOR_GRAY2RGBA,4);*/
                Bitmap resultBitmap = Bitmap.createBitmap(srcMat.cols(), srcMat.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(srcMat, resultBitmap);

                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                this.sorting(alCubie);
                this.setAllBtn(alCubie);

                Log.d("CUBIE", String.valueOf(alCubie.size()));
                for (Cubie cubie: alCubie) {
                    Log.d("CUBIE", Arrays.toString(cubie.getColor()));

                }
                imageView.setImageBitmap(resultBitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setAllBtn(List<Cubie> alCubie) {
        int index = 0;
        Log.d("alCubie", String.valueOf(alCubie.size()));
        for (Button btn : btn) {
            if (index < alCubie.size()) {
                if (alCubie.get(index).getColor() != null)
                    btn.setBackgroundColor(Color.parseColor(alCubie.get(index).getClosestColor()));
                index++;
            }
        }
    }

    private Mat findLargestRectangle(Mat original_image, Bitmap bmp) {
        Mat imgSource = original_image;

        //convert the image to black and white

        Scalar yellowL  = new Scalar(20,100,100,0);
        Scalar yellowH  = new Scalar(30,255,255,0);

        Scalar greenL  = new Scalar(46,100,100,0);
        Scalar greenH  = new Scalar(100,255,255,0);

        Scalar redL2  = new Scalar(160,100,100,0);
        Scalar redH2 = new Scalar(180,255,255,0);

        Scalar blueL  = new Scalar(101,100,100,0);
        Scalar blueH  = new Scalar(150,255,255,0);

        Scalar orangeL  = new Scalar(0,100,100,0);
        Scalar orangeH  = new Scalar(15,255,255,0);

        Scalar whiteL = new Scalar(0,0,245);
        Scalar whiteH = new Scalar(180,70,255);

        Scalar[][] colors = new Scalar[][] {{redL2,redH2},{yellowL,yellowH}, {greenL, greenH}, {blueL,blueH}, {orangeL,orangeH}, {whiteL, whiteH}};

      /*  for (EColors color : EColors.values()) {
            Mat tmp = new Mat();
            imgSource.copyTo(tmp);



            Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_RGBA2BGR);
            Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_BGR2HSV, 3);


            Core.inRange(tmp, color.getLower(), color.getHigher(), tmp);



            Core.bitwise_not(tmp, tmp);
            /*Imgproc.dilate(tmp, tmp, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1,1)));

            Imgproc.Canny(tmp, tmp, 2, 2);
            Imgproc.adaptiveThreshold(tmp, tmp,  15, Imgproc.CV_POLY_APPROX_DP,
                    Imgproc.THRESH_BINARY, 11, 12);
                   */
          /*  Imgproc.GaussianBlur(tmp, tmp,new Size(0,0),6);

            Imgproc.dilate(tmp, tmp, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9,9)));
            Imgproc.Canny (tmp, tmp, 10, 150);
            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Imgproc.findContours(tmp, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);


            if (contours.size() > 0) {
                Log.d("CONTOUR", String.valueOf(color.getBind()) + contours.size());
                MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
                MatOfPoint2f approxCurve = new MatOfPoint2f();
                Mat largest_contour = contours.get(0);
                List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
                double maxVal = 0;
                int maxValIdx = 0;
                for (int idx = 0; idx < contours.size(); idx++) {
                    temp_contour = contours.get(idx);
                    double contourarea = Imgproc.contourArea(contours.get(idx));
                    //compare this contour to the previous largest contour found
                    if (maxVal < contourarea) {
                        largest_contour = contours.get(idx);
                        maxVal = contourarea;
                        maxValIdx = idx;
                    }
                }

                List<Moments> mu = new ArrayList<Moments>(contours.size());
                for (int i = 0; i < contours.size(); i++) {
                    mu.add(i, Imgproc.moments(contours.get(i), false));
                    Moments p = mu.get(i);
                    int x = (int) (p.get_m10() / p.get_m00());
                    int y = (int) (p.get_m01() / p.get_m00());
                    Imgproc.drawMarker(imgSource, new Point(x, y), new Scalar(255, 100, 0));
                }

                Imgproc.drawContours(imgSource, contours, maxValIdx, new Scalar(0, 255, 0), 3);
            }
        }


           */


        //for (EColors color : EColors.values()) {

        Mat tmp = new Mat();
        imgSource.copyTo(tmp);
        Mat n = new Mat();


        Imgproc.cvtColor(tmp, n, Imgproc.COLOR_RGBA2GRAY);
        //Imgproc.cvtColor(tmp, n, Imgproc.COLOR_BGR2HSV, 3);

        //Core.inRange(tmp, color.getLower(), color.getHigher(), n);

        Imgproc.GaussianBlur(n, n ,new Size(7,7),1);
        Mat t = new Mat() ;
        double CannyAccThresh = Imgproc.threshold(n,t,0,255,Imgproc.THRESH_OTSU);
        double CannyThresh = 0.1* CannyAccThresh;

        //Imgproc.adaptiveThreshold(n, n, 20, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 25);
        //Imgproc.threshold(n, n, 0, 255, Imgproc.THRESH_OTSU);
        //Imgproc.threshold(n,n,0,255,Imgproc.THRESH_OTSU);
        //Core.bitwise_not(n, n);

        Imgproc.dilate(n, n, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 9)));

        Imgproc.Canny(n, n, CannyThresh,CannyAccThresh);

        //Core.bitwise_not(n, n);
        //Imgproc.cvtColor(n, n, Imgproc.COLOR_BGR2GRAY);



        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        List<MatOfPoint> largest = new ArrayList<MatOfPoint>();

        Imgproc.findContours(n, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);


        if (contours.size() > 0) {
            Log.d("CONTOUR", String.valueOf(contours.size()));
            MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            Mat largest_contour = contours.get(0);
            List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
            double maxVal = 0;
            int maxValIdx = 0;
            for (int idx = 0; idx < contours.size(); idx++) {
                temp_contour = contours.get(idx);


                double contourarea = Imgproc.contourArea(contours.get(idx));
                Point[] pts = contours.get(idx).toArray();
                for (Point p : pts) {
                    if (p.x != 0 && p.y != 0)
                        //compare this contour to the previous largest contour found
                        if (maxVal < contourarea) {

                            largest_contour = contours.get(idx);
                            maxVal = contourarea;
                            maxValIdx = idx;
                        }
                }
            }

            //Collections.reverse(largest);

            contours = contours.stream().sorted(Comparator.comparing(MatOfPoint::cols).thenComparing(MatOfPoint::rows)).collect(Collectors.toList());

            Collections.reverse(contours);
            List<Moments> mu = new ArrayList<Moments>(largest.size());
            for (int i = 0; i < 9; i++) {
                mu.add(i, Imgproc.moments(contours.get(i), false));

                Moments p = mu.get(i);
                int x = (int) (p.get_m10() / p.get_m00());
                int y = (int) (p.get_m01() / p.get_m00());

                Point[] pts = contours.get(i).toArray();
                Rect rect = Imgproc.boundingRect(contours.get(i));
                if (rect.height != n.height() && rect.width != n.width()
                    // && (Math.abs( x - oldX ) > 5 || Math.abs( oldX - x ) < 5 )
                    // && (Math.abs( y - oldY ) > 5 || Math.abs( oldY - y ) < 5 )
                        ) {
                    Imgproc.drawMarker(imgSource, new Point(x, y), new Scalar(255, 0, 0));
                    double[] colorPixel = imgSource.get(x, y);
                    int pixel = bmp.getPixel(x,y);

                    int red = Color.red(pixel);
                    int green = Color.green(pixel);
                    int blue = Color.blue(pixel);

                    Log.d("CUBI", red + " " + green + " " +blue);
                    alCubie.add(new Cubie(x,y,colorPixel));
                }
                //Imgproc.drawContours(imgSource, contours, i, new Scalar(0, 255, 0), 3);

                // AL d'Object Cube
                // Cube (x,y,color)
                // after end -> sort by x,y
            }
        }


//        }


















      /*  for (int i = 0; i < colors.length; i++) {
            Mat tmp = new Mat();
            Core.inRange(imgSource, colors[i][0], colors[i][1], tmp);
            Imgproc.GaussianBlur(tmp, tmp, new Size(3, 3), 2);
            Core.bitwise_not(tmp, tmp);

            Imgproc.Canny(tmp, tmp, 80, 85);
            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Imgproc.findContours(tmp, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
            double maxArea = -1;
            int maxAreaIdx = -1;


            MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            Mat largest_contour = contours.get(0);
            List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
            double maxVal = 0;
            int maxValIdx = 0;
            for (int idx = 0; idx < contours.size(); idx++) {
                temp_contour = contours.get(idx);
                Log.d("COORD","COORD " + i +" "+ contours.get(0).);

                double contourarea = Imgproc.contourArea(contours.get(idx));
                //compare this contour to the previous largest contour found
                if (maxVal < contourarea)
                {
                    maxVal = contourarea;
                    maxValIdx = idx;
                }
            }
            Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_BayerBG2RGB);
            Imgproc.drawContours(imgSource, contours, maxValIdx, new Scalar(0, 255, 0), 3 );
        }
*/
        //Imgproc.cvtColor(imgSource,imgSource,Imgproc.COLOR_HSV2RGB, 4);
        //Core.inRange(imgSource,redL2,redH2,imgSource);
        //convert the image to black and white does (8 bit)


        //apply gaussian blur to smoothen lines of dots
      /*  Imgproc.GaussianBlur(imgSource, imgSource, new Size(3, 3), 2);
        Core.bitwise_not(imgSource, imgSource);

        Imgproc.Canny(imgSource, imgSource, 80, 85);

        //find the contours
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(imgSource, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = -1;
        int maxAreaIdx = -1;


        MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Mat largest_contour = contours.get(0);
        List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
        double maxVal = 0;
        int maxValIdx = 0;
        for (int idx = 0; idx < contours.size(); idx++) {
            temp_contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(contours.get(idx));
            //compare this contour to the previous largest contour found
            if (maxVal < contourarea)
            {
                maxVal = contourarea;
                maxValIdx = idx;
            }
        }



        Imgproc.cvtColor(imgSource, imgSource, Imgproc.COLOR_BayerBG2RGB);
        Imgproc.drawContours(imgSource, contours, maxAreaIdx, new Scalar(0, 255, 0), 3 );
        //create the new image here using the largest detected square

        Toast.makeText(getApplicationContext(), "Largest Contour: ", Toast.LENGTH_LONG).show();
*/
        return imgSource;
    }


    private void sorting(List<Cubie> cubies) {
        for (Cubie c : cubies
                ) {
            Log.d("CUBIE", "-----> " + Arrays.toString(c.getColor()));

        }
        alCubie = cubies.stream().sorted(Comparator.comparing(Cubie::getX).thenComparing(Cubie::getY)).collect(Collectors.toList());

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewRGBA  = menu.add("Preview RGBA");
        mItemPreviewHist  = menu.add("Histograms");
        mItemPreviewCanny = menu.add("Canny");
        mItemPreviewSepia = menu.add("Sepia");
        mItemPreviewSobel = menu.add("Sobel");
        mItemPreviewZoom  = menu.add("Zoom");
        mItemPreviewPixelize  = menu.add("Pixelize");
        mItemPreviewPosterize = menu.add("Posterize");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemPreviewRGBA)
            viewMode = VIEW_MODE_RGBA;
        if (item == mItemPreviewHist)
            viewMode = VIEW_MODE_HIST;
        else if (item == mItemPreviewCanny)
            viewMode = VIEW_MODE_CANNY;
        else if (item == mItemPreviewSepia)
            viewMode = VIEW_MODE_SEPIA;
        else if (item == mItemPreviewSobel)
            viewMode = VIEW_MODE_SOBEL;
        else if (item == mItemPreviewZoom)
            viewMode = VIEW_MODE_ZOOM;
        else if (item == mItemPreviewPixelize)
            viewMode = VIEW_MODE_PIXELIZE;
        else if (item == mItemPreviewPosterize)
            viewMode = VIEW_MODE_POSTERIZE;
        return true;
    }

    public void onCameraViewStarted(int width, int height) {
        mIntermediateMat = new Mat();
        mSize0 = new Size();
        mChannels = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2) };
        mBuff = new float[mHistSizeNum];
        mHistSize = new MatOfInt(mHistSizeNum);
        mRanges = new MatOfFloat(0f, 256f);
        mMat0  = new Mat();
        mColorsRGB = new Scalar[] { new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255) };
        mColorsHue = new Scalar[] {
                new Scalar(255, 0, 0, 255),   new Scalar(255, 60, 0, 255),  new Scalar(255, 120, 0, 255), new Scalar(255, 180, 0, 255), new Scalar(255, 240, 0, 255),
                new Scalar(215, 213, 0, 255), new Scalar(150, 255, 0, 255), new Scalar(85, 255, 0, 255),  new Scalar(20, 255, 0, 255),  new Scalar(0, 255, 30, 255),
                new Scalar(0, 255, 85, 255),  new Scalar(0, 255, 150, 255), new Scalar(0, 255, 215, 255), new Scalar(0, 234, 255, 255), new Scalar(0, 170, 255, 255),
                new Scalar(0, 120, 255, 255), new Scalar(0, 60, 255, 255),  new Scalar(0, 0, 255, 255),   new Scalar(64, 0, 255, 255),  new Scalar(120, 0, 255, 255),
                new Scalar(180, 0, 255, 255), new Scalar(255, 0, 255, 255), new Scalar(255, 0, 215, 255), new Scalar(255, 0, 85, 255),  new Scalar(255, 0, 0, 255)
        };
        mWhilte = Scalar.all(255);
        mP1 = new Point();
        mP2 = new Point();

        // Fill sepia kernel
        mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
        mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
        mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
        mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);
    }

    public void onCameraViewStopped() {
        // Explicitly deallocate Mats
        if (mIntermediateMat != null)
            mIntermediateMat.release();

        mIntermediateMat = null;
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        Size sizeRgba = rgba.size();

        Mat rgbaInnerWindow;

        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;

        int left = cols / 8;
        int top = rows / 8;

        int width = cols * 3 / 4;
        int height = rows * 3 / 4;

        switch (ImageManipulationsActivity.viewMode) {
            case ImageManipulationsActivity.VIEW_MODE_RGBA:
                break;

            case ImageManipulationsActivity.VIEW_MODE_HIST:
                Mat hist = new Mat();
                int thikness = (int) (sizeRgba.width / (mHistSizeNum + 10) / 5);
                if(thikness > 5) thikness = 5;
                int offset = (int) ((sizeRgba.width - (5*mHistSizeNum + 4*10)*thikness)/2);
                // RGB
                for(int c=0; c<3; c++) {
                    Imgproc.calcHist(Arrays.asList(rgba), mChannels[c], mMat0, hist, mHistSize, mRanges);
                    Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
                    hist.get(0, 0, mBuff);
                    for(int h=0; h<mHistSizeNum; h++) {
                        mP1.x = mP2.x = offset + (c * (mHistSizeNum + 10) + h) * thikness;
                        mP1.y = sizeRgba.height-1;
                        mP2.y = mP1.y - 2 - (int)mBuff[h];
                        Imgproc.line(rgba, mP1, mP2, mColorsRGB[c], thikness);
                    }
                }
                // Value and Hue
                Imgproc.cvtColor(rgba, mIntermediateMat, Imgproc.COLOR_RGB2HSV_FULL);
                // Value
                Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[2], mMat0, hist, mHistSize, mRanges);
                Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
                hist.get(0, 0, mBuff);
                for(int h=0; h<mHistSizeNum; h++) {
                    mP1.x = mP2.x = offset + (3 * (mHistSizeNum + 10) + h) * thikness;
                    mP1.y = sizeRgba.height-1;
                    mP2.y = mP1.y - 2 - (int)mBuff[h];
                    Imgproc.line(rgba, mP1, mP2, mWhilte, thikness);
                }
                // Hue
                Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[0], mMat0, hist, mHistSize, mRanges);
                Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
                hist.get(0, 0, mBuff);
                for(int h=0; h<mHistSizeNum; h++) {
                    mP1.x = mP2.x = offset + (4 * (mHistSizeNum + 10) + h) * thikness;
                    mP1.y = sizeRgba.height-1;
                    mP2.y = mP1.y - 2 - (int)mBuff[h];
                    Imgproc.line(rgba, mP1, mP2, mColorsHue[h], thikness);
                }
                break;

            case ImageManipulationsActivity.VIEW_MODE_CANNY:
                rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
                Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 90);
                Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
                rgbaInnerWindow.release();
                break;

            case ImageManipulationsActivity.VIEW_MODE_SOBEL:
                Mat gray = inputFrame.gray();
                Mat grayInnerWindow = gray.submat(top, top + height, left, left + width);
                rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
                Imgproc.Sobel(grayInnerWindow, mIntermediateMat, CvType.CV_8U, 1, 1);
                Core.convertScaleAbs(mIntermediateMat, mIntermediateMat, 10, 0);
                Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
                grayInnerWindow.release();
                rgbaInnerWindow.release();
                break;

            case ImageManipulationsActivity.VIEW_MODE_SEPIA:
                rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
                Core.transform(rgbaInnerWindow, rgbaInnerWindow, mSepiaKernel);
                rgbaInnerWindow.release();
                break;

            case ImageManipulationsActivity.VIEW_MODE_ZOOM:
                Mat zoomCorner = rgba.submat(0, rows / 2 - rows / 10, 0, cols / 2 - cols / 10);
                Mat mZoomWindow = rgba.submat(rows / 2 - 9 * rows / 100, rows / 2 + 9 * rows / 100, cols / 2 - 9 * cols / 100, cols / 2 + 9 * cols / 100);
                Imgproc.resize(mZoomWindow, zoomCorner, zoomCorner.size(), 0, 0, Imgproc.INTER_LINEAR_EXACT);
                Size wsize = mZoomWindow.size();
                Imgproc.rectangle(mZoomWindow, new Point(1, 1), new Point(wsize.width - 2, wsize.height - 2), new Scalar(255, 0, 0, 255), 2);
                zoomCorner.release();
                mZoomWindow.release();
                break;

            case ImageManipulationsActivity.VIEW_MODE_PIXELIZE:
                rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
                Imgproc.resize(rgbaInnerWindow, mIntermediateMat, mSize0, 0.1, 0.1, Imgproc.INTER_NEAREST);
                Imgproc.resize(mIntermediateMat, rgbaInnerWindow, rgbaInnerWindow.size(), 0., 0., Imgproc.INTER_NEAREST);
                rgbaInnerWindow.release();
                break;

            case ImageManipulationsActivity.VIEW_MODE_POSTERIZE:
            /*
            Imgproc.cvtColor(rgbaInnerWindow, mIntermediateMat, Imgproc.COLOR_RGBA2RGB);
            Imgproc.pyrMeanShiftFiltering(mIntermediateMat, mIntermediateMat, 5, 50);
            Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_RGB2RGBA);
            */
                rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
                Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 90);
                rgbaInnerWindow.setTo(new Scalar(0, 0, 0, 255), mIntermediateMat);
                Core.convertScaleAbs(rgbaInnerWindow, mIntermediateMat, 1./16, 0);
                Core.convertScaleAbs(mIntermediateMat, rgbaInnerWindow, 16, 0);
                rgbaInnerWindow.release();
                break;
        }

        return rgba;
    }


    class ButtonClicker implements View.OnClickListener {
        int index = 0;
        int btnIndex = 0;
        public ButtonClicker(int i) {
            this.btnIndex = i;
        }

        @Override
        public void onClick(View v) {
            onClickBtn(btnIndex, index);
            if (index+1 == EColors.values().length)
                index = 0;
            else
                index++;
        }
    }
    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";

        MyClientTask(String addr, int port){
            dstAddress = addr;
            dstPort = port;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;

            try {
                socket = new Socket(dstAddress, dstPort);

                ByteArrayOutputStream byteArrayOutputStream =
                        new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                int bytesRead;
                InputStream inputStream = socket.getInputStream();

                /*
                 * notice:
                 * inputStream.read() will block if no data return
                 */
                while ((bytesRead = inputStream.read(buffer)) != -1){
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                }

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }finally{
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            textResponse.setText(response);
            super.onPostExecute(result);
        }

    }
}

