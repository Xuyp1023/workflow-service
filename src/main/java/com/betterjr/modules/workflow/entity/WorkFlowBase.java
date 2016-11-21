package com.betterjr.modules.workflow.entity;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.betterjr.common.entity.BetterjrEntity;
import com.betterjr.common.selectkey.SerialGenerator;
import com.betterjr.common.utils.BetterDateUtils;
import com.betterjr.common.utils.UserUtils;
import com.betterjr.modules.workflow.constant.WorkFlowConstants;

@Access(AccessType.FIELD)
@Entity
@Table(name = "t_sys_wf_base")
public class WorkFlowBase implements BetterjrEntity {
    @Id
    @Column(name = "ID", columnDefinition = "INTEGER")
    private Long id;

    @Column(name = "L_CATEGORY_ID", columnDefinition = "INTEGER")
    private Long categoryId;

    @Column(name = "C_NAME", columnDefinition = "VARCHAR")
    private String name;

    @Column(name = "C_NICKNAME", columnDefinition = "VARCHAR")
    private String nickname;

    @Column(name = "C_OPERROLE", columnDefinition = "VARCHAR")
    private String operRole;

    @Column(name = "C_OPERORG", columnDefinition = "VARCHAR")
    private String operOrg;

    @Column(name = "L_CUSTNO", columnDefinition = "INTEGER")
    private Long custNo;

    @Column(name = "C_CUSTNAME", columnDefinition = "VARCHAR")
    private String custName;

    @Column(name = "N_VERSION", columnDefinition = "INTEGER")
    private Long version;

    @Column(name = "C_IS_LATEST", columnDefinition = "CHAR")
    private String isLatest;

    @Column(name = "C_IS_PUBLISHED", columnDefinition = "CHAR")
    private String isPublished;

    @Column(name = "C_IS_DEFAULT", columnDefinition = "CHAR")
    private String isDefault;

    @Column(name = "C_IS_DISABLED", columnDefinition = "CHAR")
    private String isDisabled;

    @Column(name = "C_IS_SUBPROCESS", columnDefinition = "CHAR")
    private String isSubprocess;

    @Column(name = "C_PROCESS_ID", columnDefinition = "VARCHAR")
    private String processId;

    @Column(name = "C_MONEY_VARIABLE", columnDefinition = "VARCHAR")
    private String moneyVariable;

    @Column(name = "L_REG_OPERID", columnDefinition = "INTEGER")
    private Long regOperId;

    @Column(name = "C_REG_OPERNAME", columnDefinition = "VARCHAR")
    private String regOperName;

    @Column(name = "D_REG_DATE", columnDefinition = "VARCHAR")
    private String regDate;

    @Column(name = "T_REG_TIME", columnDefinition = "VARCHAR")
    private String regTime;

    @Column(name = "L_MODI_OPERID", columnDefinition = "INTEGER")
    private Long modiOperId;

    @Column(name = "C_MODI_OPERNAME", columnDefinition = "VARCHAR")
    private String modiOperName;

    @Column(name = "D_MODI_DATE", columnDefinition = "VARCHAR")
    private String modiDate;

    @Column(name = "T_MODI_TIME", columnDefinition = "VARCHAR")
    private String modiTime;

    @Column(name = "C_BUSIN_STATUS", columnDefinition = "VARCHAR")
    private String businStatus;

    @Column(name = "C_LAST_STATUS", columnDefinition = "VARCHAR")
    private String lastStatus;

    private static final long serialVersionUID = 1479349929428L;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(final Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(final String nickname) {
        this.nickname = nickname;
    }

    public String getOperRole() {
        return operRole;
    }

    public void setOperRole(final String operRole) {
        this.operRole = operRole;
    }

    public String getOperOrg() {
        return operOrg;
    }

    public void setOperOrg(final String operOrg) {
        this.operOrg = operOrg;
    }

    public Long getCustNo() {
        return custNo;
    }

    public void setCustNo(final Long custNo) {
        this.custNo = custNo;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(final String custName) {
        this.custName = custName;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(final Long version) {
        this.version = version;
    }

    public String getIsLatest() {
        return isLatest;
    }

    public void setIsLatest(final String isLatest) {
        this.isLatest = isLatest;
    }

    public String getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(final String isPublished) {
        this.isPublished = isPublished;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(final String isDefault) {
        this.isDefault = isDefault;
    }

    public String getIsDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(final String isDisabled) {
        this.isDisabled = isDisabled;
    }
    public String getIsSubprocess() {
        return isSubprocess;
    }

    public void setIsSubprocess(final String isSubprocess) {
        this.isSubprocess = isSubprocess;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(final String processId) {
        this.processId = processId;
    }

    public String getMoneyVariable() {
        return moneyVariable;
    }

    public void setMoneyVariable(final String moneyVariable) {
        this.moneyVariable = moneyVariable;
    }

    public Long getRegOperId() {
        return regOperId;
    }

    public void setRegOperId(final Long regOperId) {
        this.regOperId = regOperId;
    }

    public String getRegOperName() {
        return regOperName;
    }

    public void setRegOperName(final String regOperName) {
        this.regOperName = regOperName == null ? null : regOperName.trim();
    }

    public String getRegDate() {
        return regDate;
    }

    public void setRegDate(final String regDate) {
        this.regDate = regDate == null ? null : regDate.trim();
    }

    public String getRegTime() {
        return regTime;
    }

    public void setRegTime(final String regTime) {
        this.regTime = regTime == null ? null : regTime.trim();
    }

    public Long getModiOperId() {
        return modiOperId;
    }

    public void setModiOperId(final Long modiOperId) {
        this.modiOperId = modiOperId;
    }

    public String getModiOperName() {
        return modiOperName;
    }

    public void setModiOperName(final String modiOperName) {
        this.modiOperName = modiOperName == null ? null : modiOperName.trim();
    }

    public String getModiDate() {
        return modiDate;
    }

    public void setModiDate(final String modiDate) {
        this.modiDate = modiDate == null ? null : modiDate.trim();
    }

    public String getModiTime() {
        return modiTime;
    }

    public void setModiTime(final String modiTime) {
        this.modiTime = modiTime == null ? null : modiTime.trim();
    }

    public String getBusinStatus() {
        return businStatus;
    }

    public void setBusinStatus(final String businStatus) {
        this.businStatus = businStatus == null ? null : businStatus.trim();
    }

    public String getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(final String lastStatus) {
        this.lastStatus = lastStatus == null ? null : lastStatus.trim();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", categoryId=").append(categoryId);
        sb.append(", name=").append(name);
        sb.append(", nickname=").append(nickname);
        sb.append(", operRole=").append(operRole);
        sb.append(", operOrg=").append(operOrg);
        sb.append(", custNo=").append(custNo);
        sb.append(", custName=").append(custName);
        sb.append(", version=").append(version);
        sb.append(", isLatest=").append(isLatest);
        sb.append(", isPublished=").append(isPublished);
        sb.append(", isDefault=").append(isDefault);
        sb.append(", isDisabled=").append(isDisabled);
        sb.append(", isSubprocess=").append(isSubprocess);
        sb.append(", processId=").append(processId);
        sb.append(", moneyVariable=").append(moneyVariable);
        sb.append(", regOperId=").append(regOperId);
        sb.append(", regOperName=").append(regOperName);
        sb.append(", regDate=").append(regDate);
        sb.append(", regTime=").append(regTime);
        sb.append(", modiOperId=").append(modiOperId);
        sb.append(", modiOperName=").append(modiOperName);
        sb.append(", modiDate=").append(modiDate);
        sb.append(", modiTime=").append(modiTime);
        sb.append(", businStatus=").append(businStatus);
        sb.append(", lastStatus=").append(lastStatus);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        final WorkFlowBase other = (WorkFlowBase) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getCategoryId() == null ? other.getCategoryId() == null : this.getCategoryId().equals(other.getCategoryId()))
                && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
                && (this.getNickname() == null ? other.getNickname() == null : this.getNickname().equals(other.getNickname()))
                && (this.getOperRole() == null ? other.getOperRole() == null : this.getOperRole().equals(other.getOperRole()))
                && (this.getOperOrg() == null ? other.getOperOrg() == null : this.getOperOrg().equals(other.getOperOrg()))
                && (this.getCustNo() == null ? other.getCustNo() == null : this.getCustNo().equals(other.getCustNo()))
                && (this.getCustName() == null ? other.getCustName() == null : this.getCustName().equals(other.getCustName()))
                && (this.getVersion() == null ? other.getVersion() == null : this.getVersion().equals(other.getVersion()))
                && (this.getIsLatest() == null ? other.getIsLatest() == null : this.getIsLatest().equals(other.getIsLatest()))
                && (this.getIsPublished() == null ? other.getIsPublished() == null : this.getIsPublished().equals(other.getIsPublished()))
                && (this.getIsDefault() == null ? other.getIsDefault() == null : this.getIsDefault().equals(other.getIsDefault()))
                && (this.getIsDisabled() == null ? other.getIsDisabled() == null : this.getIsDisabled().equals(other.getIsDisabled()))
                && (this.getIsSubprocess() == null ? other.getIsSubprocess() == null : this.getIsSubprocess().equals(other.getIsSubprocess()))
                && (this.getProcessId() == null ? other.getProcessId() == null : this.getProcessId().equals(other.getProcessId()))
                && (this.getMoneyVariable() == null ? other.getMoneyVariable() == null : this.getMoneyVariable().equals(other.getMoneyVariable()))
                && (this.getRegOperId() == null ? other.getRegOperId() == null : this.getRegOperId().equals(other.getRegOperId()))
                && (this.getRegOperName() == null ? other.getRegOperName() == null : this.getRegOperName().equals(other.getRegOperName()))
                && (this.getRegDate() == null ? other.getRegDate() == null : this.getRegDate().equals(other.getRegDate()))
                && (this.getRegTime() == null ? other.getRegTime() == null : this.getRegTime().equals(other.getRegTime()))
                && (this.getModiOperId() == null ? other.getModiOperId() == null : this.getModiOperId().equals(other.getModiOperId()))
                && (this.getModiOperName() == null ? other.getModiOperName() == null : this.getModiOperName().equals(other.getModiOperName()))
                && (this.getModiDate() == null ? other.getModiDate() == null : this.getModiDate().equals(other.getModiDate()))
                && (this.getModiTime() == null ? other.getModiTime() == null : this.getModiTime().equals(other.getModiTime()))
                && (this.getBusinStatus() == null ? other.getBusinStatus() == null : this.getBusinStatus().equals(other.getBusinStatus()))
                && (this.getLastStatus() == null ? other.getLastStatus() == null : this.getLastStatus().equals(other.getLastStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getCategoryId() == null) ? 0 : getCategoryId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getNickname() == null) ? 0 : getNickname().hashCode());
        result = prime * result + ((getOperRole() == null) ? 0 : getOperRole().hashCode());
        result = prime * result + ((getOperOrg() == null) ? 0 : getOperOrg().hashCode());
        result = prime * result + ((getCustNo() == null) ? 0 : getCustNo().hashCode());
        result = prime * result + ((getCustName() == null) ? 0 : getCustName().hashCode());
        result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
        result = prime * result + ((getIsLatest() == null) ? 0 : getIsLatest().hashCode());
        result = prime * result + ((getIsPublished() == null) ? 0 : getIsPublished().hashCode());
        result = prime * result + ((getIsDefault() == null) ? 0 : getIsDefault().hashCode());
        result = prime * result + ((getIsDisabled() == null) ? 0 : getIsDisabled().hashCode());
        result = prime * result + ((getIsSubprocess() == null) ? 0 : getIsSubprocess().hashCode());
        result = prime * result + ((getProcessId() == null) ? 0 : getProcessId().hashCode());
        result = prime * result + ((getMoneyVariable() == null) ? 0 : getMoneyVariable().hashCode());
        result = prime * result + ((getRegOperId() == null) ? 0 : getRegOperId().hashCode());
        result = prime * result + ((getRegOperName() == null) ? 0 : getRegOperName().hashCode());
        result = prime * result + ((getRegDate() == null) ? 0 : getRegDate().hashCode());
        result = prime * result + ((getRegTime() == null) ? 0 : getRegTime().hashCode());
        result = prime * result + ((getModiOperId() == null) ? 0 : getModiOperId().hashCode());
        result = prime * result + ((getModiOperName() == null) ? 0 : getModiOperName().hashCode());
        result = prime * result + ((getModiDate() == null) ? 0 : getModiDate().hashCode());
        result = prime * result + ((getModiTime() == null) ? 0 : getModiTime().hashCode());
        result = prime * result + ((getBusinStatus() == null) ? 0 : getBusinStatus().hashCode());
        result = prime * result + ((getLastStatus() == null) ? 0 : getLastStatus().hashCode());
        return result;
    }

    /**
     * @param anWorkFlowBaseDefault
     *
     */
    public void initAddValue(final WorkFlowBase anWorkFlowBaseDefault) {
        this.id = SerialGenerator.getLongValue("WorkFlowBase.id");

        this.name = anWorkFlowBaseDefault.getName();
        this.isSubprocess = anWorkFlowBaseDefault.getIsSubprocess();

        this.isDefault = WorkFlowConstants.NOT_DEFAULT;
        this.isPublished = WorkFlowConstants.NOT_PUBLISHED;
        this.isLatest = WorkFlowConstants.NOT_LATEST;

        this.regDate = BetterDateUtils.getNumDate();
        this.regTime = BetterDateUtils.getNumTime();
        this.regOperId = UserUtils.getOperatorInfo() != null ? UserUtils.getOperatorInfo().getId() : 0L;
        this.regOperName = UserUtils.getOperatorInfo() != null ? UserUtils.getOperatorInfo().getName() : "";
        this.operOrg = UserUtils.getOperatorInfo() != null ? UserUtils.getOperatorInfo().getOperOrg() : "";

        this.modiOperId = UserUtils.getOperatorInfo() != null ? UserUtils.getOperatorInfo().getId() : 0L;
        this.modiOperName = UserUtils.getOperatorInfo() != null ? UserUtils.getOperatorInfo().getName() : "";
        this.modiDate = BetterDateUtils.getNumDate();
        this.modiTime = BetterDateUtils.getNumTime();
    }

    /**
     *
     */
    public void initModifyValue() {
        this.modiOperId = UserUtils.getOperatorInfo() != null ? UserUtils.getOperatorInfo().getId() : 0L;
        this.modiOperName = UserUtils.getOperatorInfo() != null ? UserUtils.getOperatorInfo().getName() : "";
        this.modiDate = BetterDateUtils.getNumDate();
        this.modiTime = BetterDateUtils.getNumTime();
    }
}