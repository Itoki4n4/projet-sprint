package mg.itu.controller;
import mg.itu.annotation.*;
import mg.itu.prom16.ModelAndView;

@AnnotationController()
public class EmpController {
    String name;
    String job;
    double salaire;
    public EmpController() {
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getJob() {
        return job;
    }
    public void setJob(String job) {
        this.job = job;
    }
    public double getSalaire() {
        return salaire;
    }
    public void setSalaire(double salaire) {
        this.salaire = salaire;
    }
    @Get(value = "/emp/list")
    public ModelAndView list(){
        ModelAndView mv = new ModelAndView();
        return mv;
    }
}
