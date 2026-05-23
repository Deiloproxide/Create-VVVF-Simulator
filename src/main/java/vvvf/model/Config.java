package vvvf.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import vvvf.model.Struct.PulseControl.Pulse;
import vvvf.model.Struct.PulseControl.Pulse.BaseWaveType;
import vvvf.model.Struct.PulseControl.Pulse.CarrierWaveConfiguration;
import vvvf.model.Struct.PulseControl.Pulse.PulseAlternative;
import vvvf.model.Struct.PulseControl.Pulse.PulseDataKey;
import vvvf.model.Struct.PulseControl.Pulse.PulseTypeName;

public final class Config {
    private Config() {
    }

    public static PulseTypeName[] getAvailablePulseType(int level) {
        return switch (level) {
            case 2 -> new PulseTypeName[]{PulseTypeName.ASYNC, PulseTypeName.SYNC, PulseTypeName.SHE, PulseTypeName.CHM, PulseTypeName.HO, PulseTypeName.DELTA_SIGMA};
            case 3 -> new PulseTypeName[]{PulseTypeName.ASYNC, PulseTypeName.SYNC, PulseTypeName.SHE, PulseTypeName.CHM};
            default -> new PulseTypeName[0];
        };
    }

    public static int[] getAvailablePulseCount(PulseTypeName pulseType, int level) {
        if (level == 2) {
            return switch (pulseType) {
                case SYNC -> new int[]{-1};
                case HO -> new int[]{5, 7, 9, 11, 13, 15, 17};
                case SHE -> new int[]{3, 5, 7, 9, 11, 13, 15, 17, 19, 21};
                case CHM -> new int[]{3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25};
                default -> new int[0];
            };
        }

        if (level == 3) {
            return switch (pulseType) {
                case SYNC -> new int[]{-1};
                case SHE -> new int[]{1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21};
                case CHM -> new int[]{1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21};
                default -> new int[0];
            };
        }
        return new int[0];
    }

    public static PulseAlternative[] getPulseAlternatives(Pulse pulseMode, int level) {
        return getPulseAlternatives(pulseMode.pulseType, pulseMode.pulseCount, level);
    }

    public static PulseAlternative[] getPulseAlternatives(PulseTypeName pulseType, int pulseCount, int level) {
        if (level == 3) {
            if (pulseType == PulseTypeName.SYNC) {
                return switch (pulseCount) {
                    case 1 -> new PulseAlternative[]{PulseAlternative.Default, PulseAlternative.Alt1};
                    case 5 -> alternativesDefaultToX(2, new PulseAlternative[0]);
                    default -> new PulseAlternative[]{PulseAlternative.Default};
                };
            }
            if (pulseType == PulseTypeName.ASYNC) {
                return new PulseAlternative[]{PulseAlternative.Default};
            }
            if (pulseType == PulseTypeName.CHM) {
                return switch (pulseCount) {
                    case 3 -> alternativesDefaultToX(2, new PulseAlternative[0]);
                    case 5 -> alternativesDefaultToX(4, new PulseAlternative[0]);
                    case 7 -> alternativesDefaultToX(6, new PulseAlternative[0]);
                    case 9 -> alternativesDefaultToX(7, new PulseAlternative[0]);
                    case 11 -> alternativesDefaultToX(10, new PulseAlternative[0]);
                    case 13 -> alternativesDefaultToX(14, new PulseAlternative[0]);
                    case 15 -> alternativesDefaultToX(17, new PulseAlternative[0]);
                    case 17 -> alternativesDefaultToX(19, new PulseAlternative[0]);
                    case 19 -> alternativesDefaultToX(25, new PulseAlternative[0]);
                    case 21 -> alternativesDefaultToX(22, new PulseAlternative[0]);
                    default -> new PulseAlternative[]{PulseAlternative.Default};
                };
            }
            if (pulseType == PulseTypeName.SHE) {
                return switch (pulseCount) {
                    case 3 -> alternativesDefaultToX(1, new PulseAlternative[0]);
                    default -> new PulseAlternative[]{PulseAlternative.Default};
                };
            }
            return new PulseAlternative[]{PulseAlternative.Default};
        }

        if (level == 2) {
            if (pulseType == PulseTypeName.SYNC) {
                return switch (pulseCount) {
                    case 1 -> alternativesDefaultToX(2, new PulseAlternative[0]);
                    case 3, 5, 6, 8, 9, 11, 13, 17 -> alternativesDefaultToX(1, new PulseAlternative[]{PulseAlternative.CP, PulseAlternative.Square});
                    default -> new PulseAlternative[]{PulseAlternative.Default, PulseAlternative.CP, PulseAlternative.Square};
                };
            }

            if (pulseType == PulseTypeName.ASYNC) {
                return new PulseAlternative[]{PulseAlternative.Default};
            }

            if (pulseType == PulseTypeName.CHM) {
                return switch (pulseCount) {
                    case 3 -> alternativesDefaultToX(2, new PulseAlternative[0]);
                    case 5 -> alternativesDefaultToX(3, new PulseAlternative[0]);
                    case 7 -> alternativesDefaultToX(5, new PulseAlternative[0]);
                    case 9 -> alternativesDefaultToX(8, new PulseAlternative[0]);
                    case 11 -> alternativesDefaultToX(11, new PulseAlternative[0]);
                    case 13 -> alternativesDefaultToX(13, new PulseAlternative[0]);
                    case 15 -> alternativesDefaultToX(23, new PulseAlternative[0]);
                    case 17 -> alternativesDefaultToX(11, new PulseAlternative[0]);
                    case 19 -> alternativesDefaultToX(11, new PulseAlternative[0]);
                    case 21 -> alternativesDefaultToX(13, new PulseAlternative[0]);
                    case 23 -> alternativesDefaultToX(14, new PulseAlternative[0]);
                    case 25 -> alternativesDefaultToX(20, new PulseAlternative[0]);
                    default -> new PulseAlternative[]{PulseAlternative.Default};
                };
            }

            if (pulseType == PulseTypeName.SHE) {
                return switch (pulseCount) {
                    case 3 -> alternativesDefaultToX(1, new PulseAlternative[0]);
                    case 5 -> alternativesDefaultToX(2, new PulseAlternative[0]);
                    case 7 -> alternativesDefaultToX(1, new PulseAlternative[0]);
                    case 9 -> alternativesDefaultToX(3, new PulseAlternative[0]);
                    case 11 -> alternativesDefaultToX(3, new PulseAlternative[0]);
                    case 13 -> alternativesDefaultToX(3, new PulseAlternative[0]);
                    case 15 -> alternativesDefaultToX(3, new PulseAlternative[0]);
                    case 17, 19, 21 -> alternativesDefaultToX(6, new PulseAlternative[0]);
                    default -> new PulseAlternative[]{PulseAlternative.Default};
                };
            }

            if (pulseType == PulseTypeName.HO) {
                return switch (pulseCount) {
                    case 5 -> alternativesDefaultToX(7, new PulseAlternative[0]);
                    case 7 -> alternativesDefaultToX(9, new PulseAlternative[0]);
                    case 9 -> alternativesDefaultToX(6, new PulseAlternative[0]);
                    case 11 -> alternativesDefaultToX(5, new PulseAlternative[0]);
                    case 13 -> alternativesDefaultToX(3, new PulseAlternative[0]);
                    case 15 -> alternativesDefaultToX(2, new PulseAlternative[0]);
                    default -> new PulseAlternative[]{PulseAlternative.Default};
                };
            }

            return new PulseAlternative[]{PulseAlternative.Default};
        }

        return new PulseAlternative[]{PulseAlternative.Default};
    }

    public static boolean isBaseWaveHarmonicAvailable(Pulse pulseMode, int level) {
        if (level == 2) {
            if (pulseMode.pulseType == PulseTypeName.DELTA_SIGMA) {
                return true;
            }
            if (pulseMode.baseWave.ordinal() >= BaseWaveType.SV.ordinal()) {
                return false;
            }
            if (pulseMode.alternative == PulseAlternative.CP) {
                return true;
            }
            if (pulseMode.alternative.ordinal() > PulseAlternative.Default.ordinal()) {
                return false;
            }
            if (pulseMode.pulseType == PulseTypeName.SYNC) {
                return pulseMode.pulseCount != 1;
            }
            return pulseMode.pulseType == PulseTypeName.ASYNC;
        }

        if (level == 3) {
            if (pulseMode.baseWave.ordinal() >= BaseWaveType.SV.ordinal()) {
                return false;
            }
            if (pulseMode.alternative.ordinal() > PulseAlternative.Default.ordinal()) {
                return false;
            }
            return pulseMode.pulseType == PulseTypeName.SYNC || pulseMode.pulseType == PulseTypeName.ASYNC;
        }
        return false;
    }

    public static boolean isBaseWaveChangeable(Pulse pulseMode, int level) {
        if (level == 2) {
            if (pulseMode.pulseType == PulseTypeName.DELTA_SIGMA) {
                return true;
            }
            if (pulseMode.alternative == PulseAlternative.CP) {
                return true;
            }
            if (pulseMode.alternative.ordinal() > PulseAlternative.Default.ordinal()) {
                return false;
            }
            if (pulseMode.pulseType == PulseTypeName.SYNC) {
                return pulseMode.pulseCount != 1;
            }
            return pulseMode.pulseType == PulseTypeName.ASYNC;
        }

        if (level == 3) {
            if (pulseMode.alternative.ordinal() > PulseAlternative.Default.ordinal()) {
                return false;
            }
            return pulseMode.pulseType == PulseTypeName.SYNC || pulseMode.pulseType == PulseTypeName.ASYNC;
        }
        return false;
    }

    public static boolean isDiscreteTimeAvailable(Pulse pulseMode, int level) {
        if (level == 2) {
            return !((pulseMode.pulseCount == 3 || pulseMode.pulseCount == 6 || pulseMode.pulseCount == 8)
                    && pulseMode.alternative == PulseAlternative.Alt1);
        }
        return level == 3;
    }

    public static boolean isCarrierWaveChangeable(Pulse pulseMode, int level) {
        if (level == 2) {
            if (pulseMode.pulseType == PulseTypeName.SHE || pulseMode.pulseType == PulseTypeName.CHM
                    || pulseMode.pulseType == PulseTypeName.HO || pulseMode.pulseType == PulseTypeName.DELTA_SIGMA) {
                return false;
            }
            if (pulseMode.pulseType == PulseTypeName.SYNC) {
                if (pulseMode.pulseCount == 1 && (pulseMode.alternative == PulseAlternative.Alt1 || pulseMode.alternative == PulseAlternative.Alt2)) return false;
                if (pulseMode.pulseCount == 3 && pulseMode.alternative == PulseAlternative.Alt1) return false;
                if ((pulseMode.pulseCount == 5 || pulseMode.pulseCount == 9 || pulseMode.pulseCount == 13 || pulseMode.pulseCount == 17)
                        && pulseMode.alternative == PulseAlternative.Alt1) return false;
                if (pulseMode.pulseCount == 11 && pulseMode.alternative == PulseAlternative.Alt1) return false;
                if ((pulseMode.pulseCount == 6 || pulseMode.pulseCount == 8) && pulseMode.alternative == PulseAlternative.Alt1) return false;
                if (pulseMode.alternative == PulseAlternative.CP || pulseMode.alternative == PulseAlternative.Square) return false;
            }
            return true;
        }

        if (level == 3) {
            if (pulseMode.pulseType == PulseTypeName.SHE || pulseMode.pulseType == PulseTypeName.CHM) {
                return false;
            }
            if (pulseMode.pulseType == PulseTypeName.SYNC) {
                if (pulseMode.pulseCount == 1 && pulseMode.alternative == PulseAlternative.Alt1) return false;
                if (pulseMode.pulseCount == 5 && (pulseMode.alternative == PulseAlternative.Alt1 || pulseMode.alternative == PulseAlternative.Alt2)) return false;
            }
            return true;
        }
        return false;
    }

    public static CarrierWaveConfiguration.CarrierWaveOption[] getAvailableCarrierWaveOptions(Pulse pulseMode, int level) {
        return CarrierWaveConfiguration.CarrierWaveOption.values();
    }

    public static PulseDataKey[] getAvailablePulseDataKey(Pulse pulseMode, int level) {
        if (level == 2) {
            return switch (pulseMode.pulseType) {
                case SYNC -> switch (pulseMode.pulseCount) {
                    case 3 -> pulseMode.alternative == PulseAlternative.Alt1 ? new PulseDataKey[]{PulseDataKey.Phase} : new PulseDataKey[0];
                    case 6, 8 -> pulseMode.alternative == PulseAlternative.Alt1 ? new PulseDataKey[]{PulseDataKey.PulseWidth} : new PulseDataKey[0];
                    default -> new PulseDataKey[0];
                };
                case DELTA_SIGMA -> new PulseDataKey[]{PulseDataKey.UpdateFrequency};
                default -> new PulseDataKey[0];
            };
        }

        if (level == 3) {
            return switch (pulseMode.pulseType) {
                case SYNC -> switch (pulseMode.pulseCount) {
                    case 1 -> new PulseDataKey[0];
                    case 5 -> switch (pulseMode.alternative) {
                        case Alt1, Alt2 -> new PulseDataKey[]{PulseDataKey.PulseWidth};
                        default -> new PulseDataKey[]{PulseDataKey.Dipolar};
                    };
                    default -> new PulseDataKey[]{PulseDataKey.Dipolar};
                };
                case ASYNC -> new PulseDataKey[]{PulseDataKey.Dipolar};
                default -> new PulseDataKey[0];
            };
        }

        return new PulseDataKey[0];
    }

    public static double getPulseDataKeyDefaultConstant(PulseDataKey key) {
        return switch (key) {
            case Dipolar -> -1;
            case Phase -> 0;
            case PulseWidth -> 0.2;
            case UpdateFrequency -> 440;
        };
    }

    private static PulseAlternative[] alternativesDefaultToX(int x, PulseAlternative[] custom) {
        List<PulseAlternative> out = new ArrayList<>();
        out.add(PulseAlternative.Default);
        out.addAll(Arrays.asList(custom));
        int altBase = PulseAlternative.Alt1.ordinal();
        PulseAlternative[] values = PulseAlternative.values();
        for (int i = 0; i < x; i++) {
            out.add(values[altBase + i]);
        }
        return out.toArray(new PulseAlternative[0]);
    }
}
