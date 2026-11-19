package logic;
import java.time.*;

public class SimulationClock {
private int year, month, day, hour, minute;
private boolean isSet;
private int elapsedMinutes=0;

public SimulationClock() {}

//getters
public int getYear() {
	return year;
}



public int getMonth() {
	return month;
}



public int getDay() {
	return day;
}



public int getHour() {
	return hour;
}



public int getMinute() {
	return minute;
}

public  LocalDateTime getNow() {
	//must check that clock is set before using LocalDateTime.of
			if (!this.isSet()) return null;
			
			return LocalDateTime.of(year, month, day, hour, minute);
		}

public boolean setStart(int year, int month, int day, int hour, int minute) {
	if(this.isSet()) {
		return false;
	}
	else {
		if (month>=1 && month<=12 && hour>=0 && hour<=23 && minute>=0 && minute<=59 && day>=1 && day<= daysInMonth(month,year) ) {
	this.year=year;
	this.month=month;
	this.day=day;
	this.hour=hour;
	this.minute=minute;
	isSet=true;
	return true;}
		else {return false;}
		}
}

public boolean isSet() {
	return isSet;
}

private static boolean isLeapYear (int year) {
	if (year%4==0) {
		if (year%100==0 && year%400==0) {
			return true;
		}
		if (year%100==0) {return false;}
		return true;}
	return false;
	}

private static int daysInMonth(int month, int year){
	if (isLeapYear(year) && month==2) {
		return 29;
	}
	if (month==1 || month==3 || month==5 || month==7 || month==8 || month==10 || month==12) {
		return 31;
	}
	if (month==2) { return 28;}
	
	return 30;
}

public boolean advance() {
	if (!this.isSet()) {
		return false;
	}
	minute+=1;
	elapsedMinutes+=1;
	if (minute>59) {
		hour+=1; 
		minute=0;
	}
	if (hour>23) {
		day+=1;
		hour=0;
	}
	if (day> daysInMonth(month, year)) {
		month+=1; 
		day=1;
	}
	if (month>12) {
		year+=1;
		month=1;
	}
	return true;
}
	
@Override
public String toString() {
	String dateString="";
	String timeString="";
	if(!this.isSet()) return "Not set";
	
	if (day<10)
		dateString+=0;
	dateString+=day+"/";
	
	if (month<10)
		dateString+=0;
	dateString+=month+"/"+year;
	
	if(hour<10) 
		timeString+=0;
	timeString+=hour+":";
	if (minute<10)
		timeString+=0;
	timeString+=minute;
	
	return dateString+" "+timeString;
}

public int getElapsedMinutes() {
	return elapsedMinutes;
}

public boolean restore(int year, int month, int day, int hour, int minute, int elapsedMinutes) {
	//Unlike setStart, it must work even if the clock is already set
	//Called by the system when loading saved data, so it must be allowed to overwrite the current state (unlike setStart, which is the admin's once-only action).
	if (month>=1 && month<=12 && hour>=0 && hour<=23 && minute>=0 && minute<=59 && day>=1 && day<= daysInMonth(month,year) && elapsedMinutes>=0) {
		this.year=year;
		this.month=month;
		this.day=day;
		this.hour=hour;
		this.minute=minute;
		this.elapsedMinutes=elapsedMinutes;
		isSet=true;
		return true;
	}
	return false;
}
	



}
