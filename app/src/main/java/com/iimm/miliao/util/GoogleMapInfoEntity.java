package com.iimm.miliao.util;

import java.util.List;

/**
 * Created by Administrator on 2019/5/13 0013.
 */

public class GoogleMapInfoEntity {

    /**
     * geometry : {"location":{"lat":34.790372,"lng":113.701281},"viewport":{"northeast":{"lat":34.7917197802915,"lng":113.7021977302915},"southwest":{"lat":34.7890218197085,"lng":113.6994997697085}}}
     * icon : https://maps.gstatic.com/mapfiles/place_api/icons/generic_business-71.png
     * id : 15226070b4bbc40a5fb869ec271ac0c631b5510c
     * name : 德亿时代城
     * place_id : ChIJ-3W_Kehm1zURF05O-Qsh1x0
     * plus_code : {"compound_code":"QPR2+4G 中国河南省郑州市金水区","global_code":"8P6MQPR2+4G"}
     * reference : ChIJ-3W_Kehm1zURF05O-Qsh1x0
     * scope : GOOGLE
     * types : ["point_of_interest","establishment"]
     * vicinity : 郑州市金水区东明路269号
     */

    private GeometryBean geometry;  //
    private String icon;
    private String id;
    private String name;
    private String place_id;
    private PlusCodeBean plus_code;
    private String reference;
    private String scope;
    private String vicinity;  //附近点显示
    private List<String> types;

    public GeometryBean getGeometry() {
        return geometry;
    }

    public void setGeometry(GeometryBean geometry) {
        this.geometry = geometry;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public PlusCodeBean getPlus_code() {
        return plus_code;
    }

    public void setPlus_code(PlusCodeBean plus_code) {
        this.plus_code = plus_code;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public static class GeometryBean {
        /**
         * location : {"lat":34.790372,"lng":113.701281}
         * viewport : {"northeast":{"lat":34.7917197802915,"lng":113.7021977302915},"southwest":{"lat":34.7890218197085,"lng":113.6994997697085}}
         */

        private LocationBean location;//当前点位置
        private ViewportBean viewport;

        public LocationBean getLocation() {
            return location;
        }

        public void setLocation(LocationBean location) {
            this.location = location;
        }

        public ViewportBean getViewport() {
            return viewport;
        }

        public void setViewport(ViewportBean viewport) {
            this.viewport = viewport;
        }

        public static class LocationBean {
            /**
             * lat : 34.790372
             * lng : 113.701281
             */

            private double lat;  //当前点位置
            private double lng;

            public double getLat() {
                return lat;
            }

            public void setLat(double lat) {
                this.lat = lat;
            }

            public double getLng() {
                return lng;
            }

            public void setLng(double lng) {
                this.lng = lng;
            }
        }

        public static class ViewportBean {
            /**
             * northeast : {"lat":34.7917197802915,"lng":113.7021977302915}
             * southwest : {"lat":34.7890218197085,"lng":113.6994997697085}
             */

            private NortheastBean northeast;
            private SouthwestBean southwest;

            public NortheastBean getNortheast() {
                return northeast;
            }

            public void setNortheast(NortheastBean northeast) {
                this.northeast = northeast;
            }

            public SouthwestBean getSouthwest() {
                return southwest;
            }

            public void setSouthwest(SouthwestBean southwest) {
                this.southwest = southwest;
            }

            public static class NortheastBean {
                /**
                 * lat : 34.7917197802915
                 * lng : 113.7021977302915
                 */

                private double lat;
                private double lng;

                public double getLat() {
                    return lat;
                }

                public void setLat(double lat) {
                    this.lat = lat;
                }

                public double getLng() {
                    return lng;
                }

                public void setLng(double lng) {
                    this.lng = lng;
                }
            }

            public static class SouthwestBean {
                /**
                 * lat : 34.7890218197085
                 * lng : 113.6994997697085
                 */

                private double lat;
                private double lng;

                public double getLat() {
                    return lat;
                }

                public void setLat(double lat) {
                    this.lat = lat;
                }

                public double getLng() {
                    return lng;
                }

                public void setLng(double lng) {
                    this.lng = lng;
                }
            }
        }
    }

    public static class PlusCodeBean {
        /**
         * compound_code : QPR2+4G 中国河南省郑州市金水区
         * global_code : 8P6MQPR2+4G
         */

        private String compound_code;
        private String global_code;

        public String getCompound_code() {
            return compound_code;
        }

        public void setCompound_code(String compound_code) {
            this.compound_code = compound_code;
        }

        public String getGlobal_code() {
            return global_code;
        }

        public void setGlobal_code(String global_code) {
            this.global_code = global_code;
        }
    }
}
