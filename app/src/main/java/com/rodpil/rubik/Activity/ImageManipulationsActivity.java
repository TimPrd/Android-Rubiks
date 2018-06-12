package com.rodpil.rubik.Activity;

import android.app.Activity;
import android.content.Context;
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

import com.rodpil.rubik.Cube.Cubie;
import com.rodpil.rubik.Cube.EColors;
import com.rodpil.rubik.Network.Client;
import com.rodpil.rubik.R;
import com.rodpil.rubik.Test.TestCube;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

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

public class ImageManipulationsActivity extends Activity {
    public static final int PICK_IMAGE = 1;
    public static final int VIEW_MODE_RGBA = 0;
    private static final String TAG = "OCVSample::Activity";
    Button[] btn = new Button[9];
    List<Cubie> alCubie = new ArrayList<>();
    EditText editTextAddress, editTextSecret;
    TextView textResponse;
    View.OnClickListener buttonConnectOnClickListener =
            new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    ClientTask clientTask = new ClientTask(
                            editTextAddress.getText().toString(),
                            2983);
                    clientTask.execute();
                }
            };
    private String[] COLORS = EColors.colors();

    private Button btnScan;
    private Integer[][] faces = new Integer[6][];
    private Spinner dropdown;
    private Button btnCo;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
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
        /*OPENCV INIT*/
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        editTextAddress = findViewById(R.id.address);
        editTextSecret = findViewById(R.id.port);
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
        textResponse = findViewById(R.id.response);

        dropdown = findViewById(R.id.spinner1);
        //create a list of items for the spinner.
        String[] items = new String[]{"Face 1", "Face 2", "Face 3", "Face 4", "Face 5", "Face 6"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                if (faces[position] != null) {
                    Integer[] array = faces[position];
                    int index = 0;
                    for (Button b : btn) {
                        b.setBackgroundColor(Color.parseColor(EColors.retrieveByBind(array[index]).getHex()));
                        index++;
                    }
                } else
                    for (Button b : btn)
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


        GridLayout gl = findViewById(R.id.grid);

        for (int i = 0; i < 9; i++) {
            Button btnTmp = new Button(this);
            btnTmp.setText("Cube N°" + i);
            btnTmp.setBackgroundColor(Color.parseColor("#ffffff"));
            btnTmp.setOnClickListener(new ButtonClicker(i));

            btnTmp.setWidth(0);
            btnTmp.setHeight(100);

            btn[i] = btnTmp;

            gl.addView(btnTmp);
        }

        btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        Button btnValidate = findViewById(R.id.btnValider);
        btnCo = findViewById(R.id.btnConnect);
        btnCo.setVisibility(View.GONE);

        btnValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer[] array = new Integer[9];
                int i = 0;
                for (Button b : btn) {
                    int color = ((ColorDrawable) b.getBackground()).getColor();
                    String hex = Integer.toHexString(color).substring(2, Integer.toHexString(color).length());
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
                for (Integer[] face : faces) {
                    if (face == null)
                        error = true;
                }
                if (!error)
                    btnCo.setVisibility(View.VISIBLE);

            }
        });

        Context activity = this;
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

                counter.forEach((k, val) -> {
                    String msg = (val != 9) ? "Face " + (k + 1) + " ratée" : "Face " + (k + 1) + " validée !";
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                });

                Client myClientTask = new Client(
                        editTextAddress.getText().toString(), 2983, faces, editTextSecret, activity);
                myClientTask.execute();

            }


        });



        /*Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
*/
    }

    private void executeTestFlamingo() {
        this.faces = TestCube.flamingo();
        this.btnCo.setVisibility(View.VISIBLE);
    }

    public void onClickBtn(int btnIndex, int index) {
        btn[btnIndex].setBackgroundColor(Color.parseColor(COLORS[index]));

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            Uri uri = data.getData();

            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                Mat srcMat = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8U);

                Bitmap myBitmap32 = bmp.copy(Bitmap.Config.ARGB_8888, true);

                Utils.bitmapToMat(myBitmap32, srcMat);

                srcMat = findLargestRectangle(srcMat, bmp);

                Bitmap resultBitmap = Bitmap.createBitmap(srcMat.cols(), srcMat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(srcMat, resultBitmap);

                ImageView imageView = findViewById(R.id.imageView);
                this.sorting(alCubie);
                this.setAllBtn(alCubie);

                Log.d("CUBIE", String.valueOf(alCubie.size()));
                for (Cubie cubie : alCubie) {
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

        Scalar yellowL = new Scalar(20, 100, 100, 0);
        Scalar yellowH = new Scalar(30, 255, 255, 0);

        Scalar greenL = new Scalar(46, 100, 100, 0);
        Scalar greenH = new Scalar(100, 255, 255, 0);

        Scalar redL2 = new Scalar(160, 100, 100, 0);
        Scalar redH2 = new Scalar(180, 255, 255, 0);

        Scalar blueL = new Scalar(101, 100, 100, 0);
        Scalar blueH = new Scalar(150, 255, 255, 0);

        Scalar orangeL = new Scalar(0, 100, 100, 0);
        Scalar orangeH = new Scalar(15, 255, 255, 0);

        Scalar whiteL = new Scalar(0, 0, 245);
        Scalar whiteH = new Scalar(180, 70, 255);


        Mat tmp = new Mat();
        imgSource.copyTo(tmp);
        Mat n = new Mat();


        Imgproc.cvtColor(tmp, n, Imgproc.COLOR_RGBA2GRAY);
        //Imgproc.cvtColor(tmp, n, Imgproc.COLOR_BGR2HSV, 3);

        //Core.inRange(tmp, color.getLower(), color.getHigher(), n);

        Imgproc.GaussianBlur(n, n, new Size(7, 7), 1);
        Mat t = new Mat();
        double CannyAccThresh = Imgproc.threshold(n, t, 0, 255, Imgproc.THRESH_OTSU);
        double CannyThresh = 0.1 * CannyAccThresh;

        //Imgproc.adaptiveThreshold(n, n, 20, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 25);
        //Imgproc.threshold(n, n, 0, 255, Imgproc.THRESH_OTSU);
        //Imgproc.threshold(n,n,0,255,Imgproc.THRESH_OTSU);
        //Core.bitwise_not(n, n);

        Imgproc.dilate(n, n, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 9)));

        Imgproc.Canny(n, n, CannyThresh, CannyAccThresh);

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
                    int pixel = bmp.getPixel(x, y);

                    int red = Color.red(pixel);
                    int green = Color.green(pixel);
                    int blue = Color.blue(pixel);

                    Log.d("CUBI", red + " " + green + " " + blue);
                    alCubie.add(new Cubie(x, y, colorPixel));
                }
                //Imgproc.drawContours(imgSource, contours, i, new Scalar(0, 255, 0), 3);

            }
        }

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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
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
    }

    /**
     *Sub Class to handle button Clik
     **/
    class ButtonClicker implements View.OnClickListener {
        int index = 0;
        int btnIndex = 0;

        public ButtonClicker(int i) {
            this.btnIndex = i;
        }

        @Override
        public void onClick(View v) {
            onClickBtn(btnIndex, index);
            if (index + 1 == EColors.values().length)
                index = 0;
            else
                index++;
        }
    }

    /**
     * Sub Class to handle a ClientTask (run a new thread to deals with the network)
     */
    public class ClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";

        ClientTask(String addr, int port) {
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
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                e.printStackTrace();
                response = "IOException: " + e.toString();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
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

