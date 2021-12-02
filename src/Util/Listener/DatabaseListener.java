/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util.Listener;

import Constant.Command;

/**
 *
 * @author AKBAR
 */
public interface DatabaseListener {
    void onInvalidLogin();
    void onSuccess(Command command, String... arguments);
    void onFail(Command command, String... arguments);
}
