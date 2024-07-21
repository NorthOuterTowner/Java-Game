package util;

import java.io.*;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelReader {
	
	public static final int NAMEWITHCODE=1;
	public static final int NAMEWITHOUTCODE=0;
	public static final int WITHOUTNAME=-1;
	
	private static final String file="record.xlsx";
	
	public static int search(String name,String password) {
		int ret=-1;
		double realPassword=Double.parseDouble(password);
		try {
			FileInputStream inputStream = new FileInputStream(new File(file));
			Workbook workbook = WorkbookFactory.create(inputStream);
			Sheet sheet = workbook.getSheetAt(0);
			for(Row row:sheet) {
				for(int i=0;i<2;i++) {
					String nameValue=row.getCell(0).getStringCellValue();
					double passValue=row.getCell(1).getNumericCellValue();
					if(nameValue.equals(name)&&passValue==realPassword) {
						ret=1;
						break;
					}else if(nameValue.equals(name)) {
						ret=0;
						break;
					}
				}
			}
			inputStream.close();
			workbook.close();
        }catch(IOException e) {
        	server.Log.warning(e.getMessage());
        }
		return ret;
	}
	
	public static void write(String name, String password) {
		try {
			FileInputStream inputStream = new FileInputStream(new File(file));
			Workbook workbook = WorkbookFactory.create(inputStream);
			Sheet sheet = workbook.getSheetAt(0);
			
			// Determine the next row index for writing
			int rowCount = sheet.getLastRowNum();
			Row newRow = sheet.createRow(rowCount + 1);
			
			// Write name
			Cell nameCell = newRow.createCell(0);
			nameCell.setCellValue(name);
			
			// Write password (assuming password is numeric)
			Cell passCell = newRow.createCell(1);
			passCell.setCellValue(Double.parseDouble(password));
			
			// Write other data as needed
			
			// Write to file
			FileOutputStream outputStream = new FileOutputStream(file);
			workbook.write(outputStream);
			outputStream.close();
			workbook.close();
		} catch (IOException | EncryptedDocumentException e) {
			server.Log.warning(e.getMessage());
		}
	}
}
