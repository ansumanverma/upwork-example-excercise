

CREATE TABLE  public.vehicles (
  id serial NOT NULL PRIMARY KEY,
   description character varying(255),
  item character varying(255)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.vehicles
  OWNER TO postgres;



INSERT INTO vehicles (id, item, description)
  VALUES
      (1, 'BMW',    'BMW'),
      (2, 'AUDI', 'AUDI');
      
      
