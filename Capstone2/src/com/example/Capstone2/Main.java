package com.example.Capstone2;

import android.app.Activity;
import android.os.Bundle;
import android.hardware.Camera;
import android.widget.TextView;

/* The chessboard state will be represented by a 1-D byte array of length 64.
 * byte[] chessboardstate = new byte[64];
 * each index 0,1,2,... will represent rank and file as follows
 * Rank	|	Corresponding array index
 * 	8		0	1	2	3	4	5	6	7
 * 	7		8	9	10	11	12	13	14	15
 * 	6		16	17	18	19	20	21	22	23
 * 	5		24	25	26	27	28	29	30	31
 * 	4		32	33	34	35	36	37	38	39
 * 	3		40	41	42	43	44	45	46	47
 * 	2		48	49	50	51	52	53	54	55
 * 	1		56	57	58	59	60	61	62	63
 * 			a	b	c	d	e	f	g	h	<-File
 *     					  
 *     					Robot
 * Each item in the byte array will include:
 * piece color, rank and file, or open
 * */
public class Main extends Activity {
    /** Called when the activity is first created. */
	boolean complete;
	boolean is_finished;
	Camera HDCAM;
	ImageProcessor imageProcessor;
	Server server;
	int picw;
	int pich;
	int[] pixels;
	Thread t;
	byte[] ChessBoardState = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
		tv.setText("Hello");
		System.out.println("Here");
		setContentView(tv);
    }
	private void init() {
		complete = false;
		is_finished = false;
		server = new Server();
		imageProcessor = new ImageProcessor(HDCAM);
	}

	@Override
	public void onStart() {
		super.onStart();
		init();
		t = new Thread() {
			public void run() {
				//System.out.println("take picture");
				///ChessBoardState = imageProcessor.process();
				server.waitForConnection();
				while (!is_finished) {
					
					//Temporarily commented out for testing Justin
					//server.waitForConnection();
					String in = server.read();
					
					if (in.substring(0, 2).compareToIgnoreCase("go") == 0) {
						System.out.println("take picture");
						ChessBoardState = imageProcessor.process();
					}
					if (in.substring(0, 4).compareToIgnoreCase("stop") == 0) {
						System.out.println("Finished");
						is_finished = true;
						
					}
					server.write(ChessBoardState);
					
				}
			}
		};
		System.out.println("Starting heavy duty thread");
		t.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		HDCAM.release();
	}
}