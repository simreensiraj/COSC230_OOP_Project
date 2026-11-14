package logic;

import java.lang.classfile.instruction.ReturnInstruction;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

public class SimulationClock {
private int year, month, day, hour, minute;
private boolean isSet;

public SimulationClock() {
//int attributes will be assigned values of 0 automatically by java, isSet is also false but i just wanted to write it deliberately
	isSet=false;
}

public boolean setStart(int year, int month, int day, int hour, int minute) {
	if(this.isSet()) {
		return false;
	}
	else {
	this.year=year;
	//this.month= if(month>=1 && month<=12) month: return false;
	this.day=day;
	this.hour=hour;
	this.minute=minute;
	isSet=true;
	return true;}
}

}
