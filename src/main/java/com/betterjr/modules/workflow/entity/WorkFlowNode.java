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

@Access(AccessType.FIELD)
@Entity
@Table(name = "t_sys_wf_node")
public class WorkFlowNode implements BetterjrEntity {
    @Id
    @Column(name = "ID",  columnDefinition="INTEGER" )
    private Long id;

    @Column(name = "L_BASE_ID",  columnDefinition="INTEGER" )
    private Long baseId;

    @Column(name = "N_SEQ",  columnDefinition="INTEGER" )
    private Integer seq;

    @Column(name = "C_NAME",  columnDefinition="VARCHAR" )
    private String name;

    @Column(name = "C_NICKNAME",  columnDefinition="VARCHAR" )
    private String nickname;

    @Column(name = "C_IS_DISABLED",  columnDefinition="VARCHAR" )
    private String isDisabled;

    @Column(name = "C_HANDLER",  columnDefinition="VARCHAR" )
    private String handler;

    @Column(name = "C_FORM",  columnDefinition="VARCHAR" )
    private String form;

    @Column(name = "C_TYPE",  columnDefinition="CHAR" )
    private String type;

    @Column(name = "C_OPERROLE",  columnDefinition="VARCHAR" )
    private String operRole;

    @Column(name = "C_MONEY_VARIABLE",  columnDefinition="VARCHAR" )
    private String moneyVariable;

    @Column(name = "L_REG_OPERID",  columnDefinition="INTEGER" )
    private Long regOperId;

    @Column(name = "C_REG_OPERNAME",  columnDefinition="VARCHAR" )
    private String regOperName;

    @Column(name = "D_REG_DATE",  columnDefinition="VARCHAR" )
    private String regDate;

    @Column(name = "T_REG_TIME",  columnDefinition="VARCHAR" )
    private String regTime;

    @Column(name = "L_MODI_OPERID",  columnDefinition="INTEGER" )
    private Long modiOperId;

    @Column(name = "C_MODI_OPERNAME",  columnDefinition="VARCHAR" )
    private String modiOperName;

    @Column(name = "D_MODI_DATE",  columnDefinition="VARCHAR" )
    private String modiDate;

    @Column(name = "T_MODI_TIME",  columnDefinition="VARCHAR" )
    private String modiTime;

    @Column(name = "C_BUSIN_STATUS",  columnDefinition="VARCHAR" )
    private String businStatus;

    @Column(name = "C_LAST_STATUS",  columnDefinition="VARCHAR" )
    private String lastStatus;

    private static final long serialVersionUID = 1479349929430L;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getBaseId() {
        return baseId;
    }

    public void setBaseId(final Long baseId) {
        this.baseId = baseId;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(final Integer seq) {
        this.seq = seq;
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

    public String getIsDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(final String isDisabled) {
        this.isDisabled = isDisabled;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(final String handler) {
        this.handler = handler;
    }

    public String getForm() {
        return form;
    }

    public void setForm(final String form) {
        this.form = form;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getOperRole() {
        return operRole;
    }

    public void setOperRole(final String operRole) {
        this.operRole = operRole;
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
        sb.append(", baseId=").append(baseId);
        sb.append(", seq=").append(seq);
        sb.append(", name=").append(name);
        sb.append(", nickname=").append(nickname);
        sb.append(", isDisabled=").append(isDisabled);
        sb.append(", handler=").append(handler);
        sb.append(", form=").append(form);
        sb.append(", type=").append(type);
        sb.append(", operRole=").append(operRole);
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
        final WorkFlowNode other = (WorkFlowNode) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getBaseId() == null ? other.getBaseId() == null : this.getBaseId().equals(other.getBaseId()))
                && (this.getSeq() == null ? other.getSeq() == null : this.getSeq().equals(other.getSeq()))
                && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
                && (this.getNickname() == null ? other.getNickname() == null : this.getNickname().equals(other.getNickname()))
                && (this.getIsDisabled() == null ? other.getIsDisabled() == null : this.getIsDisabled().equals(other.getIsDisabled()))
                && (this.getHandler() == null ? other.getHandler() == null : this.getHandler().equals(other.getHandler()))
                && (this.getForm() == null ? other.getForm() == null : this.getForm().equals(other.getForm()))
                && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
                && (this.getOperRole() == null ? other.getOperRole() == null : this.getOperRole().equals(other.getOperRole()))
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
        result = prime * result + ((getBaseId() == null) ? 0 : getBaseId().hashCode());
        result = prime * result + ((getSeq() == null) ? 0 : getSeq().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getNickname() == null) ? 0 : getNickname().hashCode());
        result = prime * result + ((getIsDisabled() == null) ? 0 : getIsDisabled().hashCode());
        result = prime * result + ((getHandler() == null) ? 0 : getHandler().hashCode());
        result = prime * result + ((getForm() == null) ? 0 : getForm().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getOperRole() == null) ? 0 : getOperRole().hashCode());
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
     * @param anTempWorkFlowNode
     */
    public void initCopyValue(final WorkFlowNode anTempWorkFlowNode) {
        this.id = SerialGenerator.getLongValue("WorkFlowNode.id");

        this.seq = anTempWorkFlowNode.getSeq();
        this.type = anTempWorkFlowNode.getType();
        this.form = anTempWorkFlowNode.getForm();
        this.handler = anTempWorkFlowNode.getHandler();
        this.name = anTempWorkFlowNode.getName();
        this.nickname = anTempWorkFlowNode.getNickname();
        this.operRole = anTempWorkFlowNode.getOperRole();
        this.moneyVariable = anTempWorkFlowNode.getMoneyVariable();

        this.regDate = BetterDateUtils.getNumDate();
        this.regTime = BetterDateUtils.getNumTime();
        this.regOperId = UserUtils.getOperatorInfo() != null ? UserUtils.getOperatorInfo().getId() : 0L;
        this.regOperName = UserUtils.getOperatorInfo() != null ? UserUtils.getOperatorInfo().getName() : "";

        this.modiOperId = UserUtils.getOperatorInfo() != null ? UserUtils.getOperatorInfo().getId() : 0L;
        this.modiOperName = UserUtils.getOperatorInfo() != null ? UserUtils.getOperatorInfo().getName() : "";
        this.modiDate = BetterDateUtils.getNumDate();
        this.modiTime = BetterDateUtils.getNumTime();
    }
}