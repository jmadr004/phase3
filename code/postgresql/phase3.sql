--#6 for phase 3 project.
SELECT P.seats - F.num_sold AS Available_Seats
FROM  	Plane P, FlightInfo FI, Flight F 
WHERE   P.ID = FI.plane_id AND FI.flight_id=F.fnum AND F.fnum='0' AND F.actual_departure_date='2014-05-01';


--#9 for phase 3 project.
--Passenger status may not equal flight num sold as the data was generated randomly
SELECT COUNT(R.status) AS Passenger_Status
FROM Customer C, Reservation R, Flight F
WHERE  C.id=R.cid AND R.fid=F.fnum AND R.status='R' AND F.fnum='0';

--#1
CREATE PROCEDURE find_max()
SPECIFIC find_max
CREATE LANGUAGE plpgsql
BEGIN
DECLARE plane_max_id INTEGER;
SELECT MAX(id) INTO plane_max_id FROM Plane;
END
CREATE SEQUENCE plane_id_seq START WITH 'plane_max_id';
CREATE LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION new_plane()
RETURNS "trigger" AS
$BODY$
BEGIN
NEW.id:=nextval('plane_id_seq');
RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER NPLANE BEFORE INSERT
ON id FOR EACH ROW
EXECUTE PROCEDURE new_plane();



