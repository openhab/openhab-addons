package org.openhab.binding.synopanalyzer.internal;

// https://github.com/spatialsparks/Leaflet.windbarb
public class WindBarb {
    public static class Options {
        public String fillColor = "#2B85C7";
        public int pointRadius = 8; // rayon du cercle
        public int strokeWidth = 2;
        public int strokeLength = 40; // longueur de la hampe
        public int barbSpacing = 5;
        public int barbHeight = 15;
    }

    private Options options;

    public WindBarb() {
        this.options = new Options();
    }

    public WindBarb(Options opts) {
        this.options = opts;
    }

    // rotation d'un point autour d'un centre
    private double[] rotate(double x, double y, double cx, double cy, double angleDeg) {
        double rad = Math.toRadians(angleDeg);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double dx = x - cx;
        double dy = y - cy;
        double rx = dx * cos - dy * sin + cx;
        double ry = dx * sin + dy * cos + cy;
        return new double[] { rx, ry };
    }

    public String generateSVG(double speed, double directionDeg) {
        int r = options.pointRadius;
        int sw = options.strokeWidth;
        int sl = options.strokeLength;
        int bs = options.barbSpacing;
        int bh = options.barbHeight;

        // Calcul des unités (5, 10, 50 kt)
        int s = (int) (5 * Math.round(speed / 5.0));
        int f50 = s / 50;
        int f10 = (s % 50) / 10;
        int f5 = (s % 10) / 5;

        int viewSize = 120;
        double mid = viewSize / 2.0;

        StringBuilder sb = new StringBuilder();
        sb.append("<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 ").append(viewSize).append(" ").append(viewSize)
                .append("' width='").append(viewSize).append("' height='").append(viewSize).append("'>");

        // Rotation du groupe (direction d'où vient le vent)
        sb.append("<g transform='rotate(").append(directionDeg).append(", ").append(mid).append(", ").append(mid)
                .append(")'>");

        // 1. Cercle de station
        sb.append("<circle cx='").append(mid).append("' cy='").append(mid).append("' r='").append(r).append("' fill='")
                .append(options.fillColor).append("' stroke='black' stroke-width='").append(sw).append("'/>");

        // 2. Hampe
        double lineTopY = mid - r - sl;
        sb.append("<line x1='").append(mid).append("' y1='").append(mid - r).append("' x2='").append(mid)
                .append("' y2='").append(lineTopY).append("' stroke='black' stroke-width='").append(sw)
                .append("' stroke-linecap='butt'/>");

        // Position de départ à l'extrémité
        double currentY = lineTopY;

        // --- 3. Fanions 50 kt (Triangles rectangles à coins arrondis) ---
        for (int i = 0; i < f50; i++) {
            // Pour que le bas du fanion soit parallèle, on doit décaler currentY
            // et dessiner le fanion "vers le haut" (direction l'extrémité)

            currentY += bs;

            // Le triangle rectangle
            String points = String.format(java.util.Locale.US, "%.1f,%.1f %.1f,%.1f %.1f,%.1f", mid, currentY, // Point
                                                                                                               // 1
                                                                                                               // (Attache
                                                                                                               // sur
                                                                                                               // hampe,
                                                                                                               // proche
                                                                                                               // centre)
                    mid + bh, currentY - bs, // Point 2 (Extérieur)
                    mid, currentY - bs); // Point 3 (Sommet sur hampe, proche extrémité)

            // ASTUCE : On utilise stroke-linejoin et un stroke de la même couleur pour arrondir
            sb.append("<polygon points='").append(points).append("' fill='black' stroke='black' ")
                    .append("stroke-width='").append(sw * 1).append("' stroke-linejoin='round'/>");

            // Espacement après le fanion pour la suite
            currentY += 2;
        }

        // --- 4. Espace tampon entre Fanions et Barbes ---
        if (f50 > 0) {
            currentY += (bs / 2.0);
        }

        // --- 5. Barbes 10 kt (Lignes longues à extrémités arrondies) ---
        for (int i = 0; i < f10; i++) {
            // On dessine de currentY vers (currentY - bs) pour l'angle oblique
            sb.append("<line x1='").append(mid).append("' y1='").append(currentY).append("' x2='").append(mid + bh)
                    .append("' y2='").append(currentY - bs).append("' stroke='black' stroke-width='").append(sw)
                    .append("' stroke-linecap='round'/>");

            currentY += bs;
        }

        // --- 6. Barbe 5 kt (Ligne courte à extrémités arrondies) ---
        if (f5 == 1) {
            // Si vent faible pure (5kt), on décale du sommet
            if (s == 5) {
                currentY += bs;
            }

            sb.append("<line x1='").append(mid).append("' y1='").append(currentY).append("' x2='")
                    .append(mid + (bh * 0.5)).append("' y2='").append(currentY - (bs * 0.5))
                    .append("' stroke='black' stroke-width='").append(sw).append("' stroke-linecap='round'/>");
        }

        sb.append("</g></svg>");
        return sb.toString();
    }
}