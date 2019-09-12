#define F_CPU 8000000UL									/* Define CPU clock Frequency e.g. here its 8MHz */
#include <avr/io.h>										/* Include AVR std. library file */
#include <util/delay.h>									/* Include delay header file */
#include <inttypes.h>									/* Include integer type header file */
#include <stdlib.h>										/* Include standard library file */
#include <stdio.h>										/* Include standard library file */
#include "MPU6050_res_define.h"							/* Include MPU6050 register define file */
#include "I2C_Master_H_file.h"							/* Include I2C Master header file */
#include "USART_RS232_H_file.h"							/* Include USART header file */

#define GYRO_SENSIVITY 131.0
#define ACCEL_SENSITIVITY 16384.0

float Acc_x, Acc_y, Acc_z, Temperature ,Gyro_x, Gyro_y, Gyro_z;
float Xa, Ya, Za, Xg, Yg, Zg;
float Xa_error, Ya_error, Za_error, Xg_error, Yg_error, Zg_error;

void MPU6050_Init()										/* Gyro initialization function */
{
	_delay_ms(150);										/* Power up time >100ms */
	I2C_Start_Wait(0xD0);								/* Start with device write address */
	I2C_Write(SMPLRT_DIV);								/* Write to sample rate register */
	I2C_Write(0x07);									/* 1KHz sample rate */
	I2C_Stop();

	I2C_Start_Wait(0xD0);
	I2C_Write(PWR_MGMT_1);								/* Write to power management register */
	I2C_Write(0x01);									/* X axis gyroscope reference frequency */
	I2C_Stop();

	I2C_Start_Wait(0xD0);
	I2C_Write(CONFIG);									/* Write to Configuration register */
	I2C_Write(0x00);									/* Fs = 8KHz */
	I2C_Stop();

	I2C_Start_Wait(0xD0);
	I2C_Write(GYRO_CONFIG);								/* Write to Gyro configuration register */
	I2C_Write(0x00);									/* Full scale range +/- 250 degree/C */
	I2C_Stop();
	
	I2C_Start_Wait(0xD0);
	I2C_Write(ACCEL_CONFIG);
	I2C_Write(0x00);									/* Accel Full Range = +- 2g */
	I2C_Stop();

	I2C_Start_Wait(0xD0);
	I2C_Write(INT_ENABLE);								/* Write to interrupt enable register */
	I2C_Write(0x01);
	I2C_Stop();
}

void MPU_Start_Loc()
{
	I2C_Start_Wait(0xD0);								/* I2C start with device write address */
	I2C_Write(ACCEL_XOUT_H);							/* Write start location address from where to read */
	I2C_Repeated_Start(0xD1);							/* I2C start with device read address */
}

void Read_RawValue()
{
	MPU_Start_Loc();									/* Read Gyro values */
	Acc_x = (((int)I2C_Read_Ack()<<8) | (int)I2C_Read_Ack());
	Acc_y = (((int)I2C_Read_Ack()<<8) | (int)I2C_Read_Ack());
	Acc_z = (((int)I2C_Read_Ack()<<8) | (int)I2C_Read_Ack());
	Temperature = (((int)I2C_Read_Ack()<<8) | (int)I2C_Read_Ack());
	Gyro_x = (((int)I2C_Read_Ack()<<8) | (int)I2C_Read_Ack());
	Gyro_y = (((int)I2C_Read_Ack()<<8) | (int)I2C_Read_Ack());
	Gyro_z = (((int)I2C_Read_Ack()<<8) | (int)I2C_Read_Nack());
	I2C_Stop();
}

void Convert_RawValue()
{
	// Divide raw value by sensitivity scale factor to get real values
	Xa = Acc_x/ACCEL_SENSITIVITY;
	Ya = Acc_y/ACCEL_SENSITIVITY;
	Za = Acc_z/ACCEL_SENSITIVITY;
	
	Xg = Gyro_x/GYRO_SENSIVITY;
	Yg = Gyro_y/GYRO_SENSIVITY;
	Zg = Gyro_z/GYRO_SENSIVITY;
}

void Calculate_Error()
{
	int c = 0;
	while(c != 2000)
	{
		Read_RawValue();
		Convert_RawValue();
		Xa_error += Xa;
		Ya_error += Ya;
		Za_error += (Za - 1);
		
		Xg_error += Xg;
		Yg_error += Yg;
		Zg_error += Zg;
		
		c++;
	}
	
	Xa_error /= c;
	Ya_error /= c;
	Za_error /= c;
	
	Xg_error /= c;
	Yg_error /= c;
	Zg_error /= c;
}

void Fix_Error()
{
	Xa -= Xa_error;
	Ya -= Ya_error;
	Za -= Za_error;
	
	Xg -= Xg_error;
	Yg -= Yg_error;
	Zg -= Zg_error;
}

int main()
{
	char buffer[20], float_[10];
	
	I2C_Init();											//Initialize I2C
	MPU6050_Init();										//Initialize MPU6050
	USART_Init(38400);									//Initialize USART with 38400 baud rate
	Calculate_Error();
	DDRA = 0b00000000;
	
	int d = 3;
	//char left_state[5] = OFF;
	int d_left = 0;
	int d_right = 0;
	while(1)
	{
		
		if(PINA & 0x01)
		{
			d_left++;
			if(d_left >= d)
				USART_SendString("1,");
			else USART_SendString("0,");
		}
		else if(PINA & 0x02)
		{
			d_right++;
			if(d_right >= d)
				USART_SendString("2,");
			else USART_SendString("0,");
		}
		else
		{
			USART_SendString("0,");
			d_left = 0;
			d_right = 0;
		}
		
		Read_RawValue();
		Convert_RawValue();
		Fix_Error();
		
		Xa *= 9.8;
		Ya *= 9.8;
		Za *= 9.8;
		
		//t = (Temperature/340.00)+36.53;					// Convert temperature in °/c using formula

		dtostrf( Xa, 5, 4, float_ );					// Take values in buffer to send all parameters over USART
		sprintf(buffer,"%s,",float_);
		USART_SendString(buffer);

		dtostrf( Ya, 5, 4, float_ );
		sprintf(buffer,"%s,",float_);
		USART_SendString(buffer);
		
		dtostrf( Za, 5, 4, float_ );
		sprintf(buffer,"%s,",float_);
		USART_SendString(buffer);

		dtostrf( Xg, 5, 4, float_ );
		sprintf(buffer,"%s,",float_);
		USART_SendString(buffer);

		dtostrf( Yg, 5, 4, float_ );
		sprintf(buffer,"%s,",float_);
		USART_SendString(buffer);
		
		dtostrf( Zg, 5, 4, float_ );
		sprintf(buffer,"%s\r\n",float_);
		USART_SendString(buffer);
	}
}
