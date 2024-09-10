package ltd.newbee.mall.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;

/*
 * 日期处理工具类
 */
public class DateUtil {
	private static Logger logger = Logger.getLogger(DateUtil.class);

	public static final String YYYY_MM_DD_HHmmss = "yyyy-MM-dd HH:mm:ss";
	public static final String YYYY_MM_DD = "yyyy-MM-dd";
	private static Calendar gregorianCalendar = null;
	/** 锁对象 */
	private static final Object lockObj = new Object();
	/** 存放不同的日期模板格式的sdf的Map */
	private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<String, ThreadLocal<SimpleDateFormat>>();
	public static Date now() {

		return new Date();
	}
	static {
		gregorianCalendar = new GregorianCalendar();
	}

	/**
	 * Date类型转换成String类型
	 * 
	 * @param formatDate 日期
	 * @param formatString 日期格式化定义
	 * @return dateString 日期字符串
	 */
	public static String convertDateToString(Date formatDate, String formatString) {

		if (formatDate == null) {// 默认取当前日期
			formatDate = now();
		}

		if (StringUtils.isEmpty(formatString)) {// 默认格式化【yyyy-MM-dd】
			formatString = YYYY_MM_DD;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(formatString);

		String dateString = "";
		try {
			dateString = sdf.format(formatDate);
		} catch (Exception e) {
			logger.error("错误信息：",e);
		}

		return dateString;
	}
	
	/**
	 * 将指定格式的字符串转换为日期型
	 *
	 * @param strDate - 日期
	 * @param oracleFormat  --oracle型日期格式
	 * @return 转换得到的日期
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Date stringToDate(String strDate, String oracleFormat)
	{
		if (strDate == null)
			return null;
		Hashtable h = new Hashtable();
		String javaFormat="";
		String s = oracleFormat.toLowerCase();
		if (s.indexOf("yyyy") != -1)
			h.put(new Integer(s.indexOf("yyyy")), "yyyy");
		else if (s.indexOf("yy") != -1)
			h.put(new Integer(s.indexOf("yy")), "yy");
		if (s.indexOf("mm") != -1)
			h.put(new Integer(s.indexOf("mm")), "MM");

		if (s.indexOf("dd") != -1)
			h.put(new Integer(s.indexOf("dd")), "dd");
		if (s.indexOf("hh24") != -1)
			h.put(new Integer(s.indexOf("hh24")), "HH");
		if (s.indexOf("mi") != -1)
			h.put(new Integer(s.indexOf("mi")), "mm");
		if (s.indexOf("ss") != -1)
			h.put(new Integer(s.indexOf("ss")), "ss");

		int intStart = 0;
		while (s.indexOf("-", intStart) != -1)
		{
			intStart = s.indexOf("-", intStart);
			h.put(new Integer(intStart), "-");
			intStart++;
		}

		intStart = 0;
		while (s.indexOf("/", intStart) != -1)
		{
			intStart = s.indexOf("/", intStart);
			h.put(new Integer(intStart), "/");
			intStart++;
		}

		intStart = 0;
		while (s.indexOf(" ", intStart) != -1)
		{
			intStart = s.indexOf(" ", intStart);
			h.put(new Integer(intStart), " ");
			intStart++;
		}

		intStart = 0;
		while (s.indexOf(":", intStart) != -1)
		{
			intStart = s.indexOf(":", intStart);
			h.put(new Integer(intStart), ":");
			intStart++;
		}

		if (s.indexOf("年") != -1)
			h.put(new Integer(s.indexOf("年")), "年");
		if (s.indexOf("月") != -1)
			h.put(new Integer(s.indexOf("月")), "月");
		if (s.indexOf("日") != -1)
			h.put(new Integer(s.indexOf("日")), "日");
		if (s.indexOf("时") != -1)
			h.put(new Integer(s.indexOf("时")), "时");
		if (s.indexOf("分") != -1)
			h.put(new Integer(s.indexOf("分")), "分");
		if (s.indexOf("秒") != -1)
			h.put(new Integer(s.indexOf("秒")), "秒");

		int i = 0;
		while (h.size() != 0)
		{
			Enumeration e = h.keys();
			int n = 0;
			while (e.hasMoreElements())
			{
				i = ((Integer) e.nextElement()).intValue();
				if (i >= n)
					n = i;
			}
			String temp = (String) h.get(new Integer(n));
			h.remove(new Integer(n));

			javaFormat = temp + javaFormat;
		}
		SimpleDateFormat df = new SimpleDateFormat(javaFormat);

		Date myDate = new Date();
		try
		{
			myDate = df.parse(strDate);
		}
		catch (Exception e)
		{
			//			log.error("操作异常："+e);
			return null;
		}

		return myDate;
	}
	
	/**
	 * 获取输入格式的日期字符串，字符串遵循Oracle格式
	 *
	 * @param d -  日期
	 * @param format -  指定日期格式，格式的写法为Oracle格式
	 * @return 按指定的日期格式转换后的日期字符串
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String dateToString(Date d, String format)
	{
		if (d == null)
			return "";
		Hashtable h = new Hashtable();
		String javaFormat = new String();
		String s = format.toLowerCase();
		if (s.indexOf("yyyy") != -1)
			h.put(new Integer(s.indexOf("yyyy")), "yyyy");
		else if (s.indexOf("yy") != -1)
			h.put(new Integer(s.indexOf("yy")), "yy");
		if (s.indexOf("mm") != -1)
			h.put(new Integer(s.indexOf("mm")), "MM");

		if (s.indexOf("dd") != -1)
			h.put(new Integer(s.indexOf("dd")), "dd");
		if (s.indexOf("hh24") != -1)
			h.put(new Integer(s.indexOf("hh24")), "HH");
		if (s.indexOf("mi") != -1)
			h.put(new Integer(s.indexOf("mi")), "mm");
		if (s.indexOf("ss") != -1)
			h.put(new Integer(s.indexOf("ss")), "ss");

		int intStart = 0;
		while (s.indexOf("-", intStart) != -1)
		{
			intStart = s.indexOf("-", intStart);
			h.put(new Integer(intStart), "-");
			intStart++;
		}

		intStart = 0;
		while (s.indexOf("/", intStart) != -1)
		{
			intStart = s.indexOf("/", intStart);
			h.put(new Integer(intStart), "/");
			intStart++;
		}

		intStart = 0;
		while (s.indexOf(" ", intStart) != -1)
		{
			intStart = s.indexOf(" ", intStart);
			h.put(new Integer(intStart), " ");
			intStart++;
		}

		intStart = 0;
		while (s.indexOf(":", intStart) != -1)
		{
			intStart = s.indexOf(":", intStart);
			h.put(new Integer(intStart), ":");
			intStart++;
		}

		if (s.indexOf("年") != -1)
			h.put(new Integer(s.indexOf("年")), "年");
		if (s.indexOf("月") != -1)
			h.put(new Integer(s.indexOf("月")), "月");
		if (s.indexOf("日") != -1)
			h.put(new Integer(s.indexOf("日")), "日");
		if (s.indexOf("时") != -1)
			h.put(new Integer(s.indexOf("时")), "时");
		if (s.indexOf("分") != -1)
			h.put(new Integer(s.indexOf("分")), "分");
		if (s.indexOf("秒") != -1)
			h.put(new Integer(s.indexOf("秒")), "秒");

		int i = 0;
		while (h.size() != 0)
		{
			Enumeration e = h.keys();
			int n = 0;
			while (e.hasMoreElements())
			{
				i = ((Integer) e.nextElement()).intValue();
				if (i >= n)
					n = i;
			}
			String temp = (String) h.get(new Integer(n));
			h.remove(new Integer(n));

			javaFormat = temp + javaFormat;
		}
		SimpleDateFormat df = new SimpleDateFormat(javaFormat, new DateFormatSymbols());
		return df.format(d);
	}
	
	// 获取日期转换方法
	public static Date getMonthDate(Integer day) {
		// 获取前月的第一天
		Calendar cal = Calendar.getInstance();// 获取当前日期
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, day);
		return cal.getTime();
	}

	 public void gotoLocalDate() {

		// 获取当前年月日
		LocalDate localDate = LocalDate.now();
		//构造指定的年月日
		LocalDate localDate1 = LocalDate.of(2019, 9, 10);

		// 获取年月日星期
		int year = localDate.getYear();
		int year1 = localDate.get(ChronoField.YEAR);
		Month month = localDate.getMonth();
		int month1 = localDate.get(ChronoField.MONTH_OF_YEAR);
		int day = localDate.getDayOfMonth();
		int day1 = localDate.get(ChronoField.DAY_OF_MONTH);
		DayOfWeek dayOfWeek = localDate.getDayOfWeek();
		int dayOfWeek1 = localDate.get(ChronoField.DAY_OF_WEEK);


		// 时间
		LocalTime localTime = LocalTime.of(13, 51, 10);
		LocalTime localTime1 = LocalTime.now();
		// 获取小时
		int hour = localTime.getHour();
		int hour1 = localTime.get(ChronoField.HOUR_OF_DAY);
		// 获取分
		int minute = localTime.getMinute();
		int minute1 = localTime.get(ChronoField.MINUTE_OF_HOUR);
		// 获取秒
		int second = localTime.getSecond();
		int second1 = localTime.get(ChronoField.SECOND_OF_MINUTE);


		// 日期时间
		LocalDateTime localDateTime = LocalDateTime.now();
		LocalDateTime localDateTime1 = LocalDateTime.of(2019, Month.SEPTEMBER, 10, 14, 46, 56);
		LocalDateTime localDateTime2 = LocalDateTime.of(localDate, localTime);
		LocalDateTime localDateTime3 = localDate.atTime(localTime);
		LocalDateTime localDateTime4 = localTime.atDate(localDate);
		// 获取LocalDate
		LocalDate localDate2 = localDateTime.toLocalDate();
		// 获取LocalTime
		LocalTime localTime2 = localDateTime.toLocalTime();


		// 获取秒数
		// 创建Instant对象
		Instant instant = Instant.now();
		// 获取秒数
		long currentSecond = instant.getEpochSecond();
		// 获取毫秒数
		long currentMilli = instant.toEpochMilli();
		long currentMilli2 = System.currentTimeMillis();

		//增加、减少年数、月数、天数等 以LocalDateTime为例
		LocalDateTime localDateTime11 = LocalDateTime.of(2019, Month.SEPTEMBER, 10, 14, 46, 56);
		//增加一年
		localDateTime = localDateTime11.plusYears(1);
		localDateTime = localDateTime11.plus(1, ChronoUnit.YEARS);
		//减少一个月
		localDateTime = localDateTime11.minusMonths(1);
		localDateTime = localDateTime11.minus(1, ChronoUnit.MONTHS);

		// 通过with修改某些值
		//修改年为2019
		localDateTime = localDateTime11.withYear(2020);
		//修改为2022
		localDateTime = localDateTime11.with(ChronoField.YEAR, 2022);


		//时间计算

		// 比如有些时候想知道这个月的最后一天是几号、下个周末是几号，通过提供的时间和日期API可以很快得到答案

		LocalDate localDate12 = LocalDate.now();
		LocalDate localDate13 = localDate.with(firstDayOfYear());

		//格式化时间
		LocalDate localDate14 = LocalDate.of(2019, 9, 10);
		String s1 = localDate14.format(DateTimeFormatter.BASIC_ISO_DATE);
		String s2 = localDate14.format(DateTimeFormatter.ISO_LOCAL_DATE);
		//自定义格式化
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String s3 = localDate14.format(dateTimeFormatter);

	}



	public static Date getDayBefore(Date date) {
		if (date == null) {
			return null;
		}
		gregorianCalendar.setTime(date);
		int day = gregorianCalendar.get(Calendar.DATE);
		gregorianCalendar.set(Calendar.DATE, day - 1);
		return gregorianCalendar.getTime();
	}

	public static Date getDayBeforeByNum(Date date,Integer num) {
		if (date == null) {
			return null;
		}
		gregorianCalendar.setTime(date);
		int day = gregorianCalendar.get(Calendar.DATE);
		gregorianCalendar.set(Calendar.DATE, day - num);
		return gregorianCalendar.getTime();
	}

//	public static void main(String[] args) {
//		System.out.println(convertDateToString(getDayBefore(new Date()),YYYY_MM_DD));
//
//	}
}
