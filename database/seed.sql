-- GLIMS seed / reference data.
--
-- Apply after database/schema.sql:
--   mysql -u <user> -p genlab_db < database/seed.sql
--
-- Course, section, instructor, category, and image rows below are
-- legitimate lab reference data. The `borrower` rows are synthetic
-- placeholders for local development/testing only -- the original seed
-- file shipped ~65 rows of real student names, @up.edu.ph emails, and
-- phone numbers (plus real staff login history), which should never have
-- been committed to a public repository. Do not add real personal data
-- back into a file tracked by git; load real borrower records into a
-- non-public database instead.

USE `genlab_db`;

INSERT INTO `category` VALUES
  (5470001,'Glassware/Plasticware'),
  (5470002,'Measuring and Analytical Instruments'),
  (5470003,'Lab Tools and Accessories'),
  (5470004,'Consumables and Miscellaneous'),
  (5470005,'Storage Containers'),
  (5470006,'Biological Materials'),
  (5470007,'Electrical Equipment'),
  (5470008,'Safety Equipment');

INSERT INTO `course` VALUES
  ('Zoo 102.1','Comparative Anatomy of Invertebrates Laboratory'),
  ('Bio 160.1','Ecology Laboratory'),
  ('Chem 40.1','Elementary Biochemistry Laboratory'),
  ('Bio 140.1','Elementary Genetics Laboratory'),
  ('Physics 71.1','Elementary Physics I Laboratory'),
  ('Bio 120.1','General Microbiology Laboratory'),
  ('Chem 23.1','Inorganic Analytical Chemistry Laboratory'),
  ('Zoo 131.1','Introduction to Developmental Biology of Animals Laboratory'),
  ('Physics 21.1','Introductory Physics Laboratory'),
  ('Zoo 111.1','Invertebrate Zoology Laboratory'),
  ('Chem 31.1','Organic Chemistry Laboratory'),
  ('Bot 111.1','Plant Morphoanatomy and Biodiversity Laboratory'),
  ('Bio 200','Undergraduate Thesis');

INSERT INTO `instructor` VALUES
  (10001,'Elena','Fernandez','efernandez@up.edu.ph'),
  (10002,'Victor','Santiago','vsantiago@up.edu.ph'),
  (10003,'Carla','Mendoza','cmendoza@up.edu.ph'),
  (10004,'Felipe','Castro','fcastro@up.edu.ph'),
  (10005,'Rosario','Gomez','rgomez@up.edu.ph'),
  (10006,'Miguel','Dominguez','mdominguez@up.edu.ph'),
  (10007,'Lucia','Villanueva','lvillanueva@up.edu.ph'),
  (10008,'Roberto','Ortega','rortega@up.edu.ph'),
  (10009,'Teresa','Morales','tmorales@up.edu.ph'),
  (10010,'Andres','Fuentes','afuentes@up.edu.ph');

INSERT INTO `section` VALUES
  (1,'A'),(2,'B'),(3,'C'),(4,'D'),(5,'E'),(6,'F'),(7,'G'),(8,'H'),
  (9,'I'),(10,'J'),(11,'K'),(12,'L'),(13,'M'),(14,'N'),(15,'O'),(16,'P');

INSERT INTO `course_section` VALUES
  ('Chem 31.1',11,10001),
  ('Zoo 102.1',1,10001),
  ('Bio 160.1',2,10002),
  ('Bot 111.1',12,10002),
  ('Bio 200',13,10003),
  ('Chem 40.1',3,10003),
  ('Bio 140.1',4,10004),
  ('Physics 71.1',5,10005),
  ('Bio 120.1',6,10006),
  ('Chem 23.1',7,10007),
  ('Zoo 131.1',8,10008),
  ('Physics 21.1',9,10009),
  ('Zoo 111.1',10,10010);

INSERT INTO `image` VALUES
  ('Beaker',5470001),
  ('Burette',5470001),
  ('Cuvette',5470001),
  ('Erlenmeyer Flask',5470001),
  ('Funnel',5470001),
  ('Graduated Cylinder',5470001),
  ('Test Tube',5470001),
  ('Analytical Balance',5470002),
  ('Centrifuge',5470002),
  ('Conductivity Meter',5470002),
  ('Bunsen Burner',5470003),
  ('Burette Clamp',5470003),
  ('Cryovial',5470005),
  ('Desiccator',5470005),
  ('Centrifuge Tube',5470006),
  ('Culture Flask',5470006),
  ('Petri Dish',5470006),
  ('Electrophoresis Apparatus',5470007);

-- Synthetic borrowers for local dev/testing -- not real people.
INSERT INTO `borrower` VALUES
  ('2020-00001','Sample Student One','sample.student1@up.edu.ph','09171234501','BS in Computer Science'),
  ('2020-00002','Sample Student Two','sample.student2@up.edu.ph','09171234502','BS in Biology'),
  ('2020-00003','Sample Student Three','sample.student3@up.edu.ph','09171234503','BS in Applied Mathematics'),
  ('2021-00004','Test Borrower Four','test.borrower4@up.edu.ph','09171234504','BS in Computer Science'),
  ('2021-00005','Test Borrower Five','test.borrower5@up.edu.ph','09171234505','BS in Biology'),
  ('2022-00006','Demo User Six','demo.user6@up.edu.ph','09171234506','BS in Applied Mathematics'),
  ('2022-00007','Demo User Seven','demo.user7@up.edu.ph','09171234507','BS in Computer Science'),
  ('2023-00008','Placeholder Eight','placeholder8@up.edu.ph','09171234508','BS in Biology'),
  ('2023-00009','Placeholder Nine','placeholder9@up.edu.ph','09171234509','BS in Computer Science'),
  ('2024-00010','Fixture Borrower Ten','fixture10@up.edu.ph','09171234510','BS in Applied Mathematics');
