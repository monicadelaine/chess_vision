package com.example.Capstone2;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;

public class ImageProcessor {
	
	Camera camera;
	int count=0;
	Coordinates zone1 = new Coordinates();
	Coordinates zone2 = new Coordinates();
	Coordinates zone3 = new Coordinates();
	Coordinates zone4 = new Coordinates();
	byte[] ChessBoardState = { 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u',
			'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u',
			'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u',
			'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u',
			'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u',
			'u', 'u', 'u' };
	Object lock = new Object();
	Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
		public void onShutter() {
			System.out.println("onShutter");
		}
	};
	Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			System.out.println("onPictureTaken jpeg");
			// Commented out by DJG but needs to be uncommented to
			// work in
			// real game situation
			
			FileOutputStream outStream = null;
			System.out.println("length of byte[] data =" + data.length);

			try {
				outStream = new FileOutputStream(String.format(
						"/sdcard/dcim/camera/temp"+Integer.toString(count)+".jpg", System
								.currentTimeMillis()));
				outStream.write(data);
				outStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			
			Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
			System.out.println(bmp.getConfig());
		//	Bitmap bmp = BitmapFactory
				//	.decodeFile("/sdcard/dcim/camera/temp.jpg");
			Bitmap frame = bmp.copy(Bitmap.Config.RGB_565, true);
			System.out.println(frame.isMutable());
			System.out.println("Width " + frame.getWidth() + " Height "
					+ frame.getHeight());
			
			// zone 1
			System.out.println("before calling member");
			int[] THRESHOLD_H1 = { 175, 60, 85 };
			int[] THRESHOLD_L1 = {70, 0, 0};
			int[] s1 = { 100, 0 };
			zone1 = search(s1, THRESHOLD_L1, THRESHOLD_H1, frame);
			// zone 2
			int[] THRESHOLD_H2 = { 175, 60, 85 };
			int[] THRESHOLD_L2 = {70, 0, 0};
			int[] s2 = { 525, 0 };
			zone2 = search(s2, THRESHOLD_L2, THRESHOLD_H2, frame);

			// zone 3

			int[] THRESHOLD_H3 = { 175, 60, 85 };
			int[] THRESHOLD_L3 = {70, 0, 0};
			int[] s3 = { 100, 405 };
			zone3 = search(s3, THRESHOLD_L3, THRESHOLD_H3, frame);

			// zone 4

			int[] THRESHOLD_H4 = { 175, 60, 85 };
			int[] THRESHOLD_L4 = {70, 0, 0};
			int[] s4 = { 525, 405 };
			zone4 = search(s4, THRESHOLD_L4, THRESHOLD_H4, frame);

			FileOutputStream corner_areas = null;
			try {
				corner_areas = new FileOutputStream(String.format(
						"/sdcard/dcim/camera/corner_areas.jpg", System
								.currentTimeMillis()));
				frame.compress(Bitmap.CompressFormat.JPEG, 100, corner_areas);
				corner_areas.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Black Pieces
			int[] THRESHOLD_HB = { 70, 7, 55 };
			int[] THRESHOLD_LB = { 0, 0, 0 };
			Find_Pieces(zone1, zone2, zone3, zone4, THRESHOLD_LB, THRESHOLD_HB,
					frame, ChessBoardState, 1);

			FileOutputStream black = null;
			try {
				black = new FileOutputStream(String.format(
						"/sdcard/dcim/camera/black"+Integer.toString(count)+".jpg", System
								.currentTimeMillis()));
				frame.compress(Bitmap.CompressFormat.JPEG, 100, black);
				black.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Cream Pieces
			/*
			 * int[] THRESHOLD_HW = {250,180,214}; int[] THRESHOLD_LW=
			 * {185,120,160}; Find_Pieces(zone1, zone2, zone3,zone4,
			 * THRESHOLD_LW, THRESHOLD_HW, frame, ChessBoardState, 0);
			 */

			double slope = (double) (zone4.y - zone3.y)
					/ (double) (zone4.x - zone3.x);
			for (int x = zone3.x; x < zone4.x; x++) {
				int y = (int) (slope * (x - zone3.x) + zone3.y);
				frame.setPixel(x, y, Color.WHITE);

			}

			FileOutputStream line = null;
			try {
				line = new FileOutputStream(String.format(
						"/sdcard/dcim/camera/line.jpg", System
								.currentTimeMillis()));
				frame.compress(Bitmap.CompressFormat.JPEG, 100, line);
				line.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println("Updated Chess Board State");
			for (int i = 0; i < 64; i++) {
				if (i % 8 == 0)
					System.out.println("");
				System.out.print((char) ChessBoardState[i] + " ");

			}

			synchronized(lock) {
				lock.notifyAll();
			}
			System.out.println("notified!");
			System.out.println("done Image");
			count++;
			
		}
	};

	public ImageProcessor(Camera camera) {
		this.camera = camera;
		this.camera = Camera.open();
	}

	
	public byte[] process() {
		//this.camera= Camera.open();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		camera.takePicture(shutterCallback, null, jpegCallback);
		try {
			synchronized(lock) {
				lock.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.camera.release();
		this.camera= Camera.open();
		return ChessBoardState;
	}

	private void Find_Pieces(Coordinates zone1, Coordinates zone2,
			Coordinates zone3, Coordinates zone4, int[] TLow, int[] THigh,
			Bitmap source, byte[] ChessBoard, int color_flag) {
		int dist1 = (int) Math.sqrt(Math.pow(zone2.x - zone1.x, 2)
				+ Math.pow(zone2.y - zone1.y, 2));
		int dist2 = (int) Math.sqrt(Math.pow(zone3.x - zone1.x, 2)
				+ Math.pow(zone3.y - zone1.y, 2));

		double dy1 = (double) (zone2.y - zone1.y);
		double dx1 = (double) (zone2.x - zone1.x);
		double dy2 = (double) (zone4.y - zone3.y);
		double dx2 = (double) (zone4.x - zone3.x);
		double dx = (dx1 + dx2) / (double) (2 * 8);
		double dy = (dy1 + dy2) / (double) (2 * 8);
		System.out.println("dx = " + dx);
		System.out.println("dy = " + dy);
		int sq_w = dist1 / 8;
		int sq_h = dist2 / 8;

		int chess_index = 0;

		double slope = (double) (zone4.y - zone3.y)
				/ (double) (zone4.x - zone3.x);

		double slope2 = (double) (zone2.y - zone1.y)
				/ (double) (zone2.x - zone1.x);
		double avg_slope = (slope + slope2) / (double) 2;
		// testing new method
		/*
		 * for (int y = zone3.y + sq_h / 2; y >= zone1.y && chess_index < 64; y
		 * -= sq_h) { for (int x = (int) ((y - zone3.y) / slope2 + zone3.x) +
		 * sq_w / 2; x <= zone4.x; x += sq_w) { int ty = (int) (slope * (x -
		 * zone3.x) + zone3.y) - (sq_h / 2); // frame.setPixel(x, y,
		 * Color.WHITE); if (Determine_Occupancy(x, ty, TLow, THigh, source))
		 * ChessBoard[chess_index] = 'b'; chess_index++; } }
		 */

		// better
		for (int i = 0; i < 8; i++) {
			for (int x = zone3.x + (sq_w / 2); x <= zone4.x && chess_index < 64; x += sq_w) {
				int y = (int) (avg_slope * (x - zone3.x) + zone3.y)
						- (sq_h / 2) - sq_h * i;
				// frame.setPixel(x, y, Color.WHITE);
				if (Determine_Occupancy(x, y, TLow, THigh, source))
					ChessBoard[(int) ((chess_index % 8 + 1) * 8 - 1 - Math
							.floor(chess_index / 8.00))] = 'b';
				else ChessBoard[(int) ((chess_index % 8 + 1) * 8 - 1 - Math
						.floor(chess_index / 8.00))] = 'u';
				chess_index++;
			}
		}
		//Takes board to proper orientation b/c it was mirrored.
		//Easy fix!!!
		byte[] temp_board = new byte[64];

		for(int i=7; i <64;i+=8) 
			for(int j=0; j<8; j++) 
				temp_board[(i-7)+j] = ChessBoard[i-j];
			
		for(int i =0;i<64;i++){
			ChessBoard[i]=temp_board[i];
		}
			
		/*System.out.println("Mirrored Chess Board State");
		for (int i = 0; i < 64; i++) {
			if (i % 8 == 0)
				System.out.println("");
			System.out.print((char) temp_board[i] + " ");

		}*/
		

	}

	private static boolean Determine_Occupancy(int i, int j, int[] TLow,
			int[] THigh, Bitmap source) {
		int src_pixel = 0;
		int pixel_count = 0;

		for (int a = -15; a < 15; a++) {
			for (int b = -15; b < 15; b++) {
				int tempX = i + a;
				int tempY = j + b;

				if (tempX >= source.getWidth())
					tempX = source.getWidth() - 1;
				if (tempY >= source.getHeight())
					tempY = source.getHeight() - 1;
				if (tempX < 0)
					tempX = 0;
				if (tempY < 0)
					tempY = 0;

				src_pixel = source.getPixel(tempX, tempY);

				int[] pixel = { Color.red(src_pixel), Color.green(src_pixel), Color.blue(src_pixel)};

				if ((pixel[0] <= THigh[0]) && (pixel[0] >= TLow[0])
						&& (pixel[1] <= THigh[1]) && (pixel[1] >= TLow[1])
						&& (pixel[2] <= THigh[2]) && (pixel[2] >= TLow[2])) {
					pixel_count++;
					source.setPixel(tempX, tempY, Color.RED);// for
					// black
					// pieces
					// System.out.print(pixel[0] + "\t" + pixel[1] +
					// "\t" + pixel[2]+"\t"+ i + "\t" + j + "\n");
				} else {
					source.setPixel(tempX, tempY, Color.WHITE);

				}

			}
		}
		if (pixel_count > 30) {
			// ChessBoard[(int) ((chess_index%8 +
			// 1)*8-1-Math.floor(chess_index/8.00))] = 'b';
			return true;
			// ChessBoard[chess_index] = 'b';
		} else
			return false;

	}

	private static Coordinates search(int[] start, int[] TLow, int[] THigh,
			Bitmap source) {
		Coordinates zone = new Coordinates();
		Vector<Integer> x_vals = new Vector<Integer>();
		Vector<Integer> y_vals = new Vector<Integer>();
		int x = 0;
		int total_x = 0;
		int total_y = 0;
		int count = 0;
		int avg_x = 0;
		int avg_y = 0;
		
		for (int i = start[0]; i < start[0] + 75; i++) {
			for (int j = start[1]; j < start[1] + 75; j++) {
				int src_pixel = source.getPixel(i, j);
				int[] pixel = { Color.red(src_pixel), Color.green(src_pixel) , Color.blue(src_pixel) };
				for (int k = 0; k < 3; k++) {
					if ((pixel[k] <= THigh[k]) && (pixel[k] >= TLow[k]))
						x++;
				}

				if (x == 3) {
					x_vals.add(i);
					y_vals.add(j);
					total_x += i;
					total_y += j;
					count++;
					//source.setPixel(i, j, Color.BLACK);//remove comment to see search areas
				} else {
					//source.setPixel(i, j, Color.WHITE);//remove comment to see search areas
				}

				x = 0;
			}
		}
		
		if(count !=0){
			avg_x = total_x / count;
			avg_y = total_y / count;
		}
		else System.out.print ("divide by zero");
		
		/*int temp_count=0;
		int temp_sum = 0;
		for(int i =0; i < x_vals.size(); i ++){
			if(Math.abs(avg_x-x_vals.elementAt(i)) > 15){
				x_vals.remove(i);
			}
			else {temp_sum += x_vals.elementAt(i);temp_count++;}
			//System.out.println("XDiffs " + Math.abs(avg_x-x_vals.elementAt(i)));
		}
		
		avg_x = temp_sum /temp_count;
		temp_count=0;
		temp_sum=0;
		for(int i =0; i < y_vals.size(); i ++){
			if(Math.abs(avg_y-y_vals.elementAt(i)) > 15){
				y_vals.remove(i);
				
			}
			else {temp_sum += y_vals.elementAt(i);temp_count++;}
			//System.out.println("YDiffs " + Math.abs(avg_y-y_vals.elementAt(i)));
		}
		avg_y = temp_sum /temp_count;*/
		
		
		zone.x = avg_x;
		zone.y = avg_y;
		source.setPixel(zone.x, zone.y , Color.YELLOW);
//		source.setPixel(zone.x-1, zone.y-1 , Color.YELLOW);
//		source.setPixel(zone.x+1, zone.y+1 , Color.YELLOW);
//		source.setPixel(zone.x+1, zone.y , Color.YELLOW);
//		source.setPixel(zone.x, zone.y+1 , Color.YELLOW);
//		source.setPixel(zone.x-1, zone.y , Color.YELLOW);
//		source.setPixel(zone.x, zone.y-1 , Color.YELLOW);
		return zone;

	}

}
