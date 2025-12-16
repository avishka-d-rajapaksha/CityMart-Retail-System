/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inventory.main;

public class MainLauncher {
    
    public static void main(String[] args) {
        System.out.println(">>> 1. MainLauncher Starting...");
        
        try {
            App.main(args);
        } catch (Exception e) {
            System.err.println(">>> CRITICAL ERROR DURING STARTUP <<<");
            e.printStackTrace();
        }
    }
}