package rig.com.tw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;

public class CsvExtractRenew {
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		System.out.print("請輸入檔案名稱： ");
		String a = input.next();
		String inPath = a;
		String outPath = "新" + a;
		ArrayList<String> header = new ArrayList<String>();
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> startDate = new ArrayList<String>();
		ArrayList<String> endDate = new ArrayList<String>();
				
		try {
			FileInputStream fis = new FileInputStream(inPath);
			FileOutputStream fos = new FileOutputStream(outPath);
			InputStreamReader isr  = new InputStreamReader(fis, StandardCharsets.UTF_8);
			OutputStreamWriter osw  = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
			BufferedReader brd = new BufferedReader(isr);
			BufferedWriter bwr = new BufferedWriter(osw);
			readCSV(brd, header, list, startDate, endDate);
			String eDate = getEarliestOrLatestDate(startDate, "earliest");
			String lDate = getEarliestOrLatestDate(endDate, "latest");
			getLayawayInstalments(eDate, lDate, header);
			ArrayList<ArrayList<String>> contentList = getContent(list, header);
			writeCSV(bwr, header, contentList);
			input.close();
			System.out.println(outPath + " 已轉檔完成");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void readCSV(BufferedReader brd, ArrayList<String> header, ArrayList<String> list,
			ArrayList<String> startDate, ArrayList<String> endDate) {
		try {
			String strLine = null;
			int index = 0;
			while ((strLine = brd.readLine()) != null) {
				String[] array = strLine.split(",");
				if (index == 0) {
					for (int i = 0; i < array.length; i++) {
						if (i == 0 || i == 4 || i == 9 || i == 11 || i == 24 || i == 25 || i == 26 || i == 37) {
							header.add(array[i] + ",");
						}
					}
				} else {
					for (int i = 0; i < array.length; i++) {
						if (i == 0 || i == 4 || i == 9 || i == 11 || i == 24 || i == 25 || i == 26 || i == 37) {
							list.add(array[i]);
							if (i == 25) {
								startDate.add(array[i]);
							} else if (i == 26) {
								endDate.add(array[i]);
							}
						}
					}
				}
				index++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeCSV(BufferedWriter bwr, ArrayList<String> header,
			ArrayList<ArrayList<String>> contentList) {
		try {
			for (String x : header) {
				bwr.write(x);
			}
			bwr.newLine();
			for (ArrayList<String> x : contentList) {
				for (int i = 0; i < x.size(); i++) {
					bwr.write(x.get(i));
				}
				bwr.newLine();
			}
			for (int i = 0; i < 3; i++) {
				bwr.newLine();
			}
			bwr.flush();
			bwr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getEarliestOrLatestDate(ArrayList<String> list, String str) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/mm/yyyy");
		ArrayList<Date> dates = new ArrayList<Date>();

		for (String d : list) {
			Date date;
			try {
				date = formatter.parse(d);
				dates.add(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		Date d;
		String date;

		switch (str) {
		case "earliest":
			d = Collections.min(dates);
			date = formatter.format(d);
			return date;
		case "latest":
			d = Collections.max(dates);
			date = formatter.format(d);
			return date;
		default:
			d = new Date();
			date = formatter.format(d);
			return date;
		}
	}

	public static void getLayawayInstalments(String start, String end, ArrayList<String> header) {
		String[] startDate = start.split("/");
		String[] endDate = end.split("/");
		int sM = Integer.parseInt(startDate[1]);
		int sY = Integer.parseInt(startDate[2]) - 1911;
		int eM = Integer.parseInt(endDate[1]);
		int eY = Integer.parseInt(endDate[2]) - 1911;
		do {
			if (sY != eY) {
				do {
					String y = String.valueOf(sY);
					String m = String.valueOf(sM);
					String date = y + "/" + m + ",";
					header.add(date);
					sM++;
				} while (sM <= 12);
				sM = 1;
			} else {
				do {
					if (sM < eM) {
						String y = String.valueOf(sY);
						String m = String.valueOf(sM);
						String date = y + "/" + m + ",";
						header.add(date);
					} else {
						String y = String.valueOf(sY);
						String m = String.valueOf(sM);
						String date = y + "/" + m;
						header.add(date);
					}
					sM++;
				} while (sM <= eM);
			}
			sY++;
		} while (sY <= eY);
	}

	public static ArrayList<ArrayList<String>> getContent(ArrayList<String> list, ArrayList<String> header) {
		ArrayList<ArrayList<String>> contentList = new ArrayList<ArrayList<String>>();
		ArrayList<String> data = new ArrayList<String>();
		ArrayList<String> paidUpCapital = new ArrayList<String>();// 實收額 list
		ArrayList<String> columnsSubTotal = new ArrayList<String>();// 各個攤銷額，攤銷額
		int i = 1;
		for (String x : list) {
			if (i % 8 != 0) {
				data.add(x + ",");
			} else {
				data.add(x + ",");
				paidUpCapital.add(x);// 實收額加入 list
				String[] p = (data.get(4)).split(",");
				int periods = Integer.parseInt(p[0]);// 期數
				String[] t = (data.get(7)).split(",");
				
				String instalment = "";
				String lastInstalment = "";
				
				if (t[0].contains(".")) {
					double z = Double.parseDouble(t[0]);
					int total = (int)z;
					double decimal = z - total;
					int inst = total / periods;
					instalment = String.valueOf(inst);
					double lastInst = inst + (total % periods) + decimal;
					lastInstalment = String.valueOf(lastInst);
				}
				else {
					int total = Integer.parseInt(t[0]);
					int inst = total / periods;
					instalment = String.valueOf(inst);
					int lastInst = inst + (total % periods);
					lastInstalment = String.valueOf(lastInst);
				}
				
				String[] sD = (data.get(5)).split(",");
				String start = sD[0];// 起始日期
				String[] s = start.split("/");
				int year = Integer.parseInt(s[2]) - 1911;
				String y = String.valueOf(year);
				String m = s[1];
				String dateOfRepublicEra = y + "/" + m.replaceFirst("^0+(?!$)", "");
				for (int j = 8; j < header.size(); j++) {
					String[] d = (header.get(j)).split(",");
					String date = d[0];
					if (dateOfRepublicEra.equals(date)) {
						for (int k = 1; k < periods; k++) {
							data.add(instalment + ",");
							columnsSubTotal.add(instalment);
						}
						data.add(lastInstalment + ",");
						columnsSubTotal.add(lastInstalment);
						for (int n = j + (periods - 1); n < header.size() - 1; n++) {
							data.add(",");
							columnsSubTotal.add("0");
						}
						j = header.size();
					} else {
						data.add(",");
						columnsSubTotal.add("0");
					}
				}
				contentList.add(data);
				data = new ArrayList<String>();
			}
			i++;
		}
		ArrayList<String> subTotal = subTotal(paidUpCapital, columnsSubTotal, header);
		contentList.add(subTotal);
		return contentList;
	}

	public static ArrayList<String> subTotal(ArrayList<String> paidUpCapital, ArrayList<String> columnsSubTotal,
			ArrayList<String> header) {
		ArrayList<String> eachSubTotal = new ArrayList<String>();
		for (int i = 0; i < 6; i++) {
			eachSubTotal.add(",");
		}
		eachSubTotal.add("合計,");
		double pCap = 0;
		for (String x : paidUpCapital) {
			double r = Double.parseDouble(x);
			pCap += r;
		}
		String paidCap = String.valueOf(pCap) + ",";
		eachSubTotal.add(paidCap);

		int cols = header.size() - 8;// 總共有幾期

		for (int i = 0; i < cols; i++) {
			double sum = 0;
			for (int j = i; j < columnsSubTotal.size(); j += cols) {
				double v = Double.parseDouble(columnsSubTotal.get(j));
				sum += v;
			}
			String sumReady = String.valueOf(sum) + ",";
			eachSubTotal.add(sumReady);
		}
		return eachSubTotal;
	}

}