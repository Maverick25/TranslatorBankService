/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.translator.dto;

import java.io.Serializable;
 
/**
 *
 * @author Jon
 */
public class LoanRequestDTO implements Serializable
{
    private static final long serialVersionUID = 1L;
   
    private String ssn;
    private double loanAmount;
    private int loanDuration;
    private int creditScore;
 
    public LoanRequestDTO()
    {
    }
 
    public LoanRequestDTO(String ssn, double loanAmount, int loanDuration, int creditScore)
    {
        this.ssn = ssn;
        this.loanAmount = loanAmount;
        this.loanDuration = loanDuration;
        this.creditScore = creditScore;
    }
 
    public String getSsn()
    {
        return ssn;
    }
 
    public void setSsn(String ssn)
    {
        this.ssn = ssn;
    }
 
    public double getLoanAmount()
    {
        return loanAmount;
    }
 
    public void setLoanAmount(double loanAmount)
    {
        this.loanAmount = loanAmount;
    }
 
    public int getLoanDuration()
    {
        return loanDuration;
    }
 
    public void setLoanDuration(int loanDuration)
    {
        this.loanDuration = loanDuration;
    }
 
    public int getCreditScore()
    {
        return creditScore;
    }
 
    public void setCreditScore(int creditScore)
    {
        this.creditScore = creditScore;
    }

    @Override
    public String toString() {
        return "LoanRequestDTO{" + "ssn=" + ssn + ", loanAmount=" + loanAmount + ", loanDuration=" + loanDuration + ", creditScore=" + creditScore + '}';
    }
    
    
}
