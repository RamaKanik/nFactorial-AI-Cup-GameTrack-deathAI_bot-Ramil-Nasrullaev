

import java.util.ArrayList;
import java.util.Scanner;
import java.awt.geom.Point2D;
import java.util.concurrent.TimeUnit;

public class deathAI {
    /** Simple representation for a puck. */
    static class Puck {

        Point2D pos;

        Point2D vel;

        int color;
    };

    /** Simple representation for a bumper. */
    static class Bumper {
        Point2D pos;

        Point2D vel;
    };

    /** Simple representation for a sled. */
    static class Sled {
        Point2D pos;

        double dir;
    };

    /** How much acceleration we are willing to spend on each component. */
    private static final double ACCEL = 8.0 / Math.sqrt( 2 );

    /** Return the value of a, clamped to the [ b, c ] range */
    private static double clamp( double a, double b, double c ) {
        if ( a < b )
            return b;
        if ( a > c )
            return c;
        return a;
    }

    /** Return a new vector containing the sum of a and b. */
    static Point2D sum( Point2D a, Point2D b ) {
        return new Point2D.Double( a.getX() + b.getX(),
                a.getY() + b.getY() );
    }

    /** Return a new vector containing the difference between a and b. */
    static Point2D diff( Point2D a, Point2D b ) {
        return new Point2D.Double( a.getX() - b.getX(),
                a.getY() - b.getY() );
    }

    /** Return a new vector containing a scaled by scaling factor s. */
    static Point2D scale( Point2D a, double s ) {
        return new Point2D.Double( a.getX() * s, a.getY() * s );
    }

    /** Return the magnitude of vector a. */
    static double mag( Point2D a ) {
        return Math.sqrt( a.getX() * a.getX() + a.getY() * a.getY() );
    }

    /** Return a new vector containing normalized version of a. */
    static Point2D norm( Point2D a ) {
        double m = mag( a );
        return new Point2D.Double( a.getX() / m,
                a.getY() / m );
    }

    /** Return a ccw perpendicular vector for a. */
    static Point2D perp( Point2D a ) {
        return new Point2D.Double( -a.getY(), a.getX() );
    }

    /** Return the dot product of a and b. */
    static double dot( Point2D a, Point2D b ) {
        return a.getX() * b.getX() + a.getY() * b.getY();
    }

    /** Return the cross product of a and b. */
    static double cross( Point2D a, Point2D b ) {
        return a.getX() * b.getY() - a.getY() * b.getX();
    }

    /** One dimensional function to help compute acceleration vectors. Return an
     acceleration that can be applied to a bumper at pos and moving
     with velocity vel to get it to target.  The alim parameter puts
     a limit on the acceleration available. */
    private static double moveTo( double pos, double vel, double target,
                                  double alim ) {
        double dist = target - pos;

        if ( Math.abs( dist ) < 0.01 )
            return clamp( -vel, -alim, alim );

        double steps = Math.ceil(( -1 + Math.sqrt(1 + 8.0 * Math.abs(dist) / alim))
                / 2.0);
        if ( steps < 1 )
            steps = 1;

        double accel = 2 * dist / ( ( steps + 1 ) * steps );

        double ivel = accel * steps;

        return clamp( ivel - vel, -alim, alim );
    }

    public static void main( String[] arg ) throws InterruptedException {
        // List of current sled, bumper and puck locations.
        ArrayList< Puck > plist = new ArrayList< Puck >();
        ArrayList< Bumper > blist = new ArrayList< Bumper >();
        ArrayList< Sled > slist = new ArrayList< Sled >();

        Scanner in = new Scanner( System.in );

        int moveCount = 0;

        int[] target = { -3, -3 };

        int[] ttimer = { 0, 0 };

        int tnum = in.nextInt();
        while ( tnum >= 0 ) {
            int n = in.nextInt();
            plist.clear();
            for ( int i = 0; i < n; i++ ) {
                Puck p = new Puck();
                double x = in.nextDouble();
                double y = in.nextDouble();
                p.pos = new Point2D.Double( x, y );
                x = in.nextDouble();
                y = in.nextDouble();
                p.vel = new Point2D.Double( x, y );
                p.color = in.nextInt();
                plist.add( p );
            }

            n = in.nextInt();
            blist.clear();
            for ( int i = 0; i < n; i++ ) {
                Bumper b = new Bumper();
                double x = in.nextDouble();
                double y = in.nextDouble();
                b.pos = new Point2D.Double( x, y );
                x = in.nextDouble();
                y = in.nextDouble();
                b.vel = new Point2D.Double( x, y );
                blist.add( b );
            }

            n = in.nextInt();
            slist.clear();
            for ( int i = 0; i < n; i++ ) {
                Sled s = new Sled();
                double x = in.nextDouble();
                double y = in.nextDouble();
                s.pos = new Point2D.Double( x, y );
                s.dir = in.nextDouble();
                slist.add( s );

                int ts = in.nextInt();
                for ( int j = 0; j < ts; j++ ) {
                    in.nextDouble();
                    in.nextDouble();
                }
            }

            for ( int i = 0; i < 2; i++ ) {
                Point2D tdest = new Point2D.Double( 100, i == 0 ? 300 : 500 );

                Bumper bumper = blist.get( i );
                if ( ttimer[ i ] <= 0 ) {
                    target[ i ] = -1;
                    for ( int j = 0; j < plist.size(); j++ ) {
                        if ( plist.get( j ).color == 2 &&
                                plist.get( j ).pos.distance( tdest ) > 120 &&
                                Math.abs( plist.get( j ).pos.getX() - 400 ) < 340 &&
                                Math.abs( plist.get( j ).pos.getY() - 400 ) < 340 &&
                                ( target[ i ] < 0 ||
                                        plist.get( j ).pos.distance( bumper.pos ) <
                                                plist.get( target[ i ] ).pos.distance( bumper.pos ) ) )
                            target[ i ] = j;
                    }

                    if ( target[ i ] >= 0 )
                        ttimer[ i ] = 20;
                }

                if ( ttimer[ i ] > 0 ) {
                    Point2D tpos = plist.get( target[ i ] ).pos;

                    double dist = tpos.distance( bumper.pos );
                    Point2D a1 = scale( diff( tpos, bumper.pos ), 1.0 / dist );
                    Point2D a2 = perp( a1 );

                    double v1 = dot( a1, bumper.vel );
                    double v2 = dot( a2, bumper.vel );

                    double f1 = 0;
                    double f2 = 0;

                    Point2D tdir = diff( tdest, tpos );

                    double dprod = dot( a1, norm( tdir ) );
                    if ( dprod < 0.8 ) {
                        if ( dist > 100 ) {
                            f1 = ACCEL;
                        } else if ( dist < 50 ) {
                            f1 = -ACCEL;
                        } else {
                            f1 = clamp( -v1, -ACCEL, ACCEL );
                        }

                        double cdist = Math.acos( dprod ) * dist;
                        if ( cross( tdir, a1 ) > 0 ) {
                            f2 = ACCEL;
                        } else {
                            f2 = -ACCEL;
                        }
                    } else {
                        a1 = norm( tdir );
                        a2 = perp( a1 );

                        v1 = dot( a1, bumper.vel );
                        v2 = dot( a2, bumper.vel );

                        Point2D bdisp = diff( bumper.pos, tpos );
                        double p1 = dot( a1, bdisp );
                        double p2 = dot( a2, bdisp );

                        double tdist = mag( tdir );

                        double a = 0.7;
                        double b = 0.7;
                        double c = -tdist;

                        double steps = ( -b + Math.sqrt( b * b - 4 * a * c ) ) / ( 2 * a );

                        double vel = steps * 0.8;

                        f1 = clamp( vel - v1, -ACCEL, ACCEL );
                        f2 = moveTo( p2, v2, 0.0, ACCEL );

                        if ( p1 + v1 + f1 > -13 )
                            ttimer[ i ] = 1;
                    }

                    Point2D force = sum( scale( a1, f1 ), scale( a2, f2 ) );

                    System.out.printf( "%.4f %.4f ", force.getX(), force.getY() );

                    ttimer[ i ]--;
                } else {
                    System.out.printf( "%.10f 0.0 ", 8.0 );
                }
            }


            if ( moveCount % 90 < 40 ) {
                System.out.printf( "%.6f\n", -Math.PI * 2 / 40);
            } else {
                System.out.printf( "%.6f\n", Math.PI * 2 / 40);
            }

            tnum = in.nextInt();
            moveCount++;
        }
    }
}
