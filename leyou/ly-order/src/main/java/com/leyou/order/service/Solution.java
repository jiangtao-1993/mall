package com.leyou.order.service;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

class Solution {
    //
    public int[] twoSum(int[] nums, int target) {
        //这是最古老的方法
        /* int[] res = new int[2];

        for (int i = 0; i < nums.length; i++) {
            for (int j = i+1; j < nums.length; j++) {
                if((nums[i]+nums[j])==target){
                    res[0]=i;
                    res[1]=j;
                }
            }
        }
        //顺序理顺
        Arrays.sort(res);*/
        int[] res = new int[2];
        //键为数组值,值为数组下标
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            if (map.containsKey(nums[i])) {
                res[0] = i;
                res[1] = map.get(target - nums[i]);
            }
            map.put(target - nums[i], i);
        }

        return res;
    }

    //给出一个 32 位的有符号整数，你需要将这个整数中每位上的数字进行反转。
    public int reverse(int x) {
        StringBuilder sb = new StringBuilder(Integer.valueOf(x).toString());
        int i;
        if (x >= 0) {

            try {
                i = Integer.parseInt(sb.reverse().toString());
            } catch (NumberFormatException e) {
                return 0;
            }
            return i;
        } else {
            try {
                i = Integer.parseInt(sb.reverse().substring(0, sb.length() - 1));
            } catch (NumberFormatException e) {
                return 0;
            }
            return -i;
        }

    }

    //判断一个整数是否是回文数。回文数是指正序（从左向右）和倒序（从右向左）读都是一样的整数。
    public boolean isPalindrome(int x) {
        String s1 = Integer.toString(x);
        String s2 = new StringBuilder(s1).reverse().toString();
        return s1.equals(s2);
    }

    //罗马数字转整数
    public int romanToInt(String s) {
        HashMap<Character, Integer> col = new HashMap<>();

        col.put('I', 1);
        col.put('V', 5);
        col.put('X', 10);
        col.put('L', 50);
        col.put('C', 100);
        col.put('D', 500);
        col.put('M', 1000);

        int a = 0;
        //首先建立一个HashMap来映射符号和值，然后对字符串从左到右来，如果当前字符代表的值大于等于其右边，就加上该值；否则就减去该值。
        //以此类推到最左边的数，最终得到的结果即是答案
        // IV 4 VI 6  IX 9 XI 11
        char[] charArray = s.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if (i == (charArray.length - 1)) {
                a = a + col.get(charArray[i]);
            } else {
                if (col.get(charArray[i]) >= col.get(charArray[i + 1])) {
                    a = a + col.get(charArray[i]);
                } else {
                    a = a - col.get(charArray[i]);
                }
            }
        }
        return a;
    }

    //编写一个函数来查找字符串数组中的最长公共前缀。如果不存在公共前缀，返回空字符串 ""。
    public static String longestCommonPrefix(String[] strs) {
        //找到最短的字符串
        Arrays.sort(strs);
        //将最短的字符串转换为数组
        StringBuilder commonPrefix = new StringBuilder();
        char[] shortStringArr = strs[strs.length - 1].toCharArray();
        for (int i = 0; i < shortStringArr.length; i++) {
            char c = shortStringArr[i];
            int count = 0;
            for (int j = 0; j < strs.length; j++) {
                if (strs[j].charAt(i) == c) {
                    count++;
                }
            }
            if (count == strs.length) {
                commonPrefix.append(c);
            }
        }
        return commonPrefix.toString();

    }

    public static void OptionalTest() {
        String a = "ww";
//        if(a!=null)
        if (Optional.ofNullable(a).isPresent()) {
            System.out.println("不为空");
        } else {
            System.out.println("为null");
        }
    }

    public static void main(String[] args) {
        /*String[] strings = {"flower", "flow", "flight"};
        System.out.println(longestCommonPrefix(strings));*/
        OptionalTest();
    }


}