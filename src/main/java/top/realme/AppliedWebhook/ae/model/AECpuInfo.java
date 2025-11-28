package top.realme.AppliedWebhook.ae.model;

import javax.annotation.Nullable;

public record AECpuInfo (boolean isBusy, String selectionMode, @Nullable AEJobInfo jobInfo) {
}
