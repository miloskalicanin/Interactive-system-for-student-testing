/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.teststudent.network;

public class RetrofitException extends RuntimeException {

    private static final long serialVersionUID = -3409018913850673308L;
    private final int statusCode;

    public RetrofitException(String detailMessage, int statusCode) {
        super(detailMessage);
        this.statusCode = statusCode;
    }

    public RetrofitException(String detailMessage, Throwable throwable, int statusCode) {
        super(detailMessage, throwable);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
